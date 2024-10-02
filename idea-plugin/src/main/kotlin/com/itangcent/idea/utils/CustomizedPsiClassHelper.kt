package com.itangcent.idea.utils

import com.google.inject.Inject
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.impl.source.PsiFieldImpl
import com.itangcent.common.constant.Attrs
import com.itangcent.common.kit.KVUtils
import com.itangcent.common.logger.Log
import com.itangcent.common.utils.*
import com.itangcent.idea.plugin.api.export.AdditionalField
import com.itangcent.idea.plugin.api.export.core.ClassExportRuleKeys
import com.itangcent.idea.plugin.api.export.swagger.ApiAnnotationUtil
import com.itangcent.idea.plugin.api.export.swagger.DefaultApiAnnotationResolver
import com.itangcent.idea.plugin.settings.EventRecords
import com.itangcent.intellij.extend.toPrettyString
import com.itangcent.intellij.jvm.AccessibleField
import com.itangcent.intellij.jvm.PsiExpressionResolver
import com.itangcent.intellij.jvm.duck.DuckType
import com.itangcent.intellij.jvm.element.ExplicitClass
import com.itangcent.intellij.psi.*
import com.itangcent.utils.isCollections
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * support rules:
 * 1. field.required
 * 2. field.default.value
 */
open class CustomizedPsiClassHelper : ContextualPsiClassHelper() {

    @Inject
    private lateinit var psiExpressionResolver: PsiExpressionResolver
    @Inject
    protected lateinit var defaultApiAnnotationResolver: DefaultApiAnnotationResolver
    @Inject
    protected lateinit var apiAnnotationUtil: ApiAnnotationUtil

    override fun afterParseField(
        accessibleField: AccessibleField,
        resourcePsiClass: ExplicitClass,
        resolveContext: ResolveContext,
        fields: MutableMap<String, Any?>,
    ) {
        //compute `field.required`
        ruleComputer.computer(ClassExportRuleKeys.FIELD_REQUIRED, accessibleField)?.let { required ->
            fields.sub(Attrs.REQUIRED_ATTR)[accessibleField.jsonFieldName()] = required
        }

        //compute `field.default.value`
        val defaultValue = ruleComputer.computer(ClassExportRuleKeys.FIELD_DEFAULT_VALUE, accessibleField)
        if (defaultValue.isNullOrEmpty()) {
            accessibleField.field?.psi()?.let { field ->
                field.initializer?.let { psiExpressionResolver.process(it) }?.toPrettyString()
                    ?.let { fields.sub(Attrs.DEFAULT_VALUE_ATTR)[accessibleField.jsonFieldName()] = it }
            }
        } else {
            fields.sub(Attrs.DEFAULT_VALUE_ATTR)[accessibleField.jsonFieldName()] = defaultValue
            parseAsFieldValue(defaultValue)
                ?.also { KVUtils.useFieldAsAttr(it, Attrs.DEFAULT_VALUE_ATTR) }
                ?.let { populateFieldValue(accessibleField.jsonFieldName(), accessibleField.jsonFieldType(), fields, it) }
        }

        //compute `field.demo`
        val demoValue = ruleComputer.computer(
            ClassExportRuleKeys.FIELD_DEMO,
            accessibleField
        )
        if (demoValue.notNullOrBlank()) {
            fields.sub(Attrs.DEMO_ATTR)[accessibleField.jsonFieldName()] = demoValue
            demoValue?.let { parseAsFieldValue(it) }
                ?.also { KVUtils.useFieldAsAttr(it, Attrs.DEMO_ATTR) }
                ?.let { populateFieldValue(accessibleField.jsonFieldName(), accessibleField.jsonFieldType(), fields, it) }
        }

        super.afterParseField(accessibleField, resourcePsiClass, resolveContext, fields)

        collectApiInfo(accessibleField,resourcePsiClass,fields)
    }

    private fun collectApiInfo(
        accessibleField: AccessibleField,
        resourcePsiClass: ExplicitClass,
        fields: MutableMap<String, Any?>
    ) {
        val fieldPsi = accessibleField.psi
        fields[Attrs.CLASS_NAME_ATTR] = resourcePsiClass.psi().name
        fields[Attrs.QUALIFIED_CLASS_NAME_ATTR] = resourcePsiClass.psi().qualifiedName
        fields.sub(Attrs.JAVA_TYPE_ATTR)[accessibleField.jsonFieldName()]=accessibleField.type.canonicalText()
        if(fieldPsi is PsiFieldImpl) {
            apiAnnotationUtil.collectJsonBodyFieldAnnotationInfo(fields, fieldPsi)
        }
    }

    override fun resolveAdditionalField(
        additionalField: AdditionalField,
        context: PsiElement,
        resolveContext: ResolveContext,
        fields: MutableMap<String, Any?>,
    ) {
        super.resolveAdditionalField(additionalField, context, resolveContext, fields)
        val fieldName = additionalField.name!!
        fields.sub(Attrs.REQUIRED_ATTR)[fieldName] = additionalField.required
        fields.sub(Attrs.DEFAULT_VALUE_ATTR)[fieldName] = additionalField.defaultValue
    }

    protected fun parseAsFieldValue(
        valueText: String
    ): Any? {
        return try {
            GsonUtils.fromJson<Any>(valueText)?.takeIf { it.isCollections() && !it.isOriginal() }
                ?.let { it.copyUnsafe() }
        } catch (e: Exception) {
            null
        }
    }

    protected fun populateFieldValue(
        fieldName: String,
        fieldType: DuckType,
        fields: MutableMap<String, Any?>,
        fieldValue: Any
    ) {
        var oldValue = fields[fieldName]
        if (oldValue is ObjectHolder) {
            oldValue = oldValue.getOrResolve()
        }
        if (oldValue == fieldValue) {
            return
        }
        if (oldValue.isOriginal()) {
            fields[fieldName] = fieldValue
        } else {
            fields[fieldName] = oldValue.copy()
            fields.merge(fieldName, fieldValue)
        }
    }

    override fun resolveEnumOrStatic(
        context: PsiElement,
        cls: PsiClass?,
        property: String?,
        defaultPropertyName: String,
        valueTypeHandle: ((DuckType) -> Unit)?,
    ): ArrayList<HashMap<String, Any?>>? {
        EventRecords.record(EventRecords.ENUM_RESOLVE)
        return super.resolveEnumOrStatic(context, cls, property, defaultPropertyName, valueTypeHandle)
    }

    override fun ignoreField(psiField: PsiField): Boolean {
        if (configReader.first("ignore_static_field")?.asBool() == false) {
            return false
        }
        return super.ignoreField(psiField)
    }

    companion object : Log()

    override fun getTypeObject(duckType: DuckType?, context: PsiElement, option: Int): Any? {
        val resolveContext = getResolveContext().withOption(option)
        val result = doGetTypeObject(duckType, context, resolveContext).getOrResolveV2()
        if (resolveContext.blocked()) {
            logger.error("The complexity of ${duckType?.canonicalText()} has exceeded the limit. It is blocked due to ${resolveContext.blockInfo()}")
        }
        return result
    }

    private fun ObjectHolder?.getOrResolveV2(): Any? {
        this ?: return null
        if (this.notResolved()) {
            val unResolvedObjectHolders = collectUnResolvedObjectHoldersAsListV2()
            for (unResolvedObjectHolder in unResolvedObjectHolders) {
                unResolvedObjectHolder.resolve(null)
            }
            if (this.notResolved()) {
                SimpleContext().with(this, null) {
                    this.resolve(it)
                    if (this.resolved()) {
                        this.onResolve(it)
                    }
                }
            }
        }
        return this.getObject()
    }

    private fun ObjectHolder.collectUnResolvedObjectHoldersAsListV2(): LinkedList<ObjectHolder> {
        val unResolvedObjectHolders = LinkedList<ObjectHolder>()
        this.collectUnResolvedObjectHolders(UnResolvedObjectHoldersAsListCollectorV2(unResolvedObjectHolders))
        return unResolvedObjectHolders
    }

    private class UnResolvedObjectHoldersAsListCollectorV2(val unResolvedObjectHolders: LinkedList<ObjectHolder>) :
            (ObjectHolder) -> Unit {
        private val ids = HashSet<Long>()

        override fun invoke(objectHolder: ObjectHolder) {
            if (ids.add(objectHolder.id)) {
                unResolvedObjectHolders.addFirst(objectHolder)
                objectHolder.collectUnResolvedObjectHolders(this)
            }
        }
    }
}