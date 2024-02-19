package com.itangcent.idea.plugin.api.export.swagger

import com.google.inject.Inject
import com.google.inject.Singleton
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.source.PsiClassImpl
import com.intellij.psi.impl.source.PsiFieldImpl
import com.intellij.psi.impl.source.PsiParameterImpl
import com.itangcent.common.constant.Attrs
import com.itangcent.common.utils.sub
import java.lang.annotation.ElementType

@Singleton
class ApiAnnotationUtil {
    @Inject
    private lateinit var defaultApiAnnotationResolver: DefaultApiAnnotationResolver

    fun collectFieldAnnotationInfo(map: MutableMap<String, Any?>, paramPsi: PsiParameterImpl) {
        val schemaInfo = defaultApiAnnotationResolver.findSchema(paramPsi)
        schemaInfo?.let { map[Attrs.SCHEMA_ATTR] = it }

        val maxInfo = defaultApiAnnotationResolver.findMax(paramPsi)
        maxInfo?.let { map[Attrs.MAX_ATTR]=it }

        val sizeInfo = defaultApiAnnotationResolver.findSize(paramPsi)
        sizeInfo?.let { map[Attrs.SIZE_ATTR]=it }

        val minInfo = defaultApiAnnotationResolver.findMin(paramPsi)
        minInfo?.let { map[Attrs.MIN_ATTR]=it }

        val emailInfo = defaultApiAnnotationResolver.findEmail(paramPsi)
        emailInfo?.let { map[Attrs.EMAIL_ATTR]=it }
    }

    fun collectClassAnnotationInfo(map: MutableMap<String, Any?>,psiClass: PsiClassImpl){
        val tagInfo = defaultApiAnnotationResolver.findTag(psiClass)
        tagInfo?.let { map[Attrs.TAG_ATTR] = it }
    }

    fun collectMethodAnnotationInfo(map: MutableMap<String, Any?>, psiMethod: PsiMethod) {
        val operationInfo = defaultApiAnnotationResolver.findOperation(psiMethod)
        operationInfo?.let { map[Attrs.OPERATION_ATTR] = it }

        val apiResponsesInfo = defaultApiAnnotationResolver.findApiResponses(psiMethod)
        apiResponsesInfo?.let { map[Attrs.API_RESPONSES_ATTR] = it }

        val apiResponseInfo = defaultApiAnnotationResolver.findApiResponse(psiMethod)
        apiResponseInfo?.let { map[Attrs.API_RESPONSE_ATTR] = it }

        val deprecatedInfo = defaultApiAnnotationResolver.findDeprecated(psiMethod)
        deprecatedInfo?.let { map[Attrs.DEPRECATED_ATTR] = it }
    }

    fun collectJsonBodyFieldAnnotationInfo(map: MutableMap<String, Any?>, fieldPsi: PsiFieldImpl) {
        val schemaInfo = defaultApiAnnotationResolver.findSchema(fieldPsi)
        val fieldName = fieldPsi.name
        schemaInfo?.let { map.sub(Attrs.SCHEMA_ATTR)[fieldName] = it }

        val minInfo = defaultApiAnnotationResolver.findMin(fieldPsi)
        minInfo?.let { map.sub(Attrs.MIN_ATTR)[fieldName] = it }

        val maxInfo = defaultApiAnnotationResolver.findMax(fieldPsi)
        maxInfo?.let { map.sub(Attrs.MAX_ATTR)[fieldName] = it }

        val emailInfo = defaultApiAnnotationResolver.findEmail(fieldPsi)
        emailInfo?.let { map.sub(Attrs.MAX_ATTR)[fieldName] = it }

        val sizeInfo = defaultApiAnnotationResolver.findSize(fieldPsi)
        sizeInfo?.let { map.sub(Attrs.SIZE_ATTR)[fieldName] = it }
    }
}