package com.itangcent.idea.utils

import com.google.inject.Inject
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.itangcent.common.constant.Attrs
import com.itangcent.common.kit.KVUtils
import com.itangcent.common.logger.Log
import com.itangcent.common.utils.*
import com.itangcent.idea.plugin.api.export.AdditionalField
import com.itangcent.idea.plugin.api.export.core.ClassExportRuleKeys
import com.itangcent.idea.plugin.api.export.swagger.DefaultSwaggerAnnotationResolver
import com.itangcent.idea.plugin.api.export.swagger.SwaggerClassName
import com.itangcent.idea.plugin.api.export.swagger.ValidationClassName
import com.itangcent.idea.plugin.settings.EventRecords
import com.itangcent.intellij.extend.toPrettyString
import com.itangcent.intellij.jvm.AccessibleField
import com.itangcent.intellij.jvm.AnnotationHelper
import com.itangcent.intellij.jvm.PsiExpressionResolver
import com.itangcent.intellij.jvm.duck.DuckType
import com.itangcent.intellij.jvm.element.ExplicitClass
import com.itangcent.intellij.psi.ObjectHolder
import com.itangcent.intellij.psi.ResolveContext
import com.itangcent.intellij.psi.computer
import com.itangcent.intellij.psi.getOrResolve
import com.itangcent.utils.isCollections

/**
 * support rules:
 * 1. field.required
 * 2. field.default.value
 */
open class CustomizedPsiClassHelper : ContextualPsiClassHelper() {

    @Inject
    private lateinit var psiExpressionResolver: PsiExpressionResolver
    @Inject
    protected lateinit var defaultSwaggerAnnotationResolver: DefaultSwaggerAnnotationResolver

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
        fields.sub(Attrs.JAVA_TYPE_ATTR)[accessibleField.jsonFieldName()]=accessibleField.field!!.getType().name()

        val schemaInfo = defaultSwaggerAnnotationResolver.findSchema(fieldPsi)
        if (schemaInfo != null) {
            fields.sub(Attrs.SCHEMA_ATTR)[accessibleField.jsonFieldName()] = schemaInfo
        }

        val minInfo = defaultSwaggerAnnotationResolver.findMin(fieldPsi)
        if (schemaInfo != null) {
            fields.sub(Attrs.MIN_ATTR)[accessibleField.jsonFieldName()] = minInfo
        }

        val maxInfo = defaultSwaggerAnnotationResolver.findMax(fieldPsi)
        if (schemaInfo != null) {
            fields.sub(Attrs.MAX_ATTR)[accessibleField.jsonFieldName()] = maxInfo
        }

        val emailInfo = defaultSwaggerAnnotationResolver.findEmail(fieldPsi)
        if (schemaInfo != null) {
            fields.sub(Attrs.EMAIL_ATTR)[accessibleField.jsonFieldName()] = emailInfo
        }

        val sizeInfo = defaultSwaggerAnnotationResolver.findSize(fieldPsi)
        if (schemaInfo != null) {
            fields.sub(Attrs.SIZE_ATTR)[accessibleField.jsonFieldName()] = sizeInfo
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
}