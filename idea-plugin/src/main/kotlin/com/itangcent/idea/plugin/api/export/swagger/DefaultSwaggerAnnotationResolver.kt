package com.itangcent.idea.plugin.api.export.swagger

import com.google.inject.Inject
import com.google.inject.Singleton
import com.intellij.psi.PsiElement
import com.itangcent.intellij.jvm.AnnotationHelper


@Singleton
class DefaultSwaggerAnnotationResolver {

    @Inject
    private lateinit var annotationHelper: AnnotationHelper
    fun findTag(psiElement: PsiElement): Map<String, Any?>? {
        return annotationHelper.findAnnMap(psiElement,SwaggerClassName.TAG_ANNOTATION)
    }

    fun findOperation(psiElement: PsiElement): Map<String,Any?>? {
        return annotationHelper.findAnnMap(psiElement,SwaggerClassName.OPERATION_ANNOTATION)
    }

    fun findApiResponses(psiElement: PsiElement): Map<String,Any?>? {
        return annotationHelper.findAnnMap(psiElement,SwaggerClassName.API_RESPONSES_ANNOTATION)
    }

    fun findApiResponse(psiElement: PsiElement): Map<String,Any?>? {
        return annotationHelper.findAnnMap(psiElement,SwaggerClassName.API_RESPONSE_ANNOTATION)
    }

    fun findSchema(psiElement: PsiElement): Map<String,Any?>? {
        return annotationHelper.findAnnMap(psiElement,SwaggerClassName.SCHEMA_ANNOTATION)
    }

    fun findMin(psiElement: PsiElement): Map<String,Any?>? {
        return annotationHelper.findAnnMap(psiElement,ValidationClassName.MIN_ANNOTATION)
    }

    fun findSize(psiElement: PsiElement): Map<String,Any?>? {
        return annotationHelper.findAnnMap(psiElement,ValidationClassName.SIZE_ANNOTATION)
    }

    fun findMax(psiElement: PsiElement): Map<String,Any?>? {
        return annotationHelper.findAnnMap(psiElement,ValidationClassName.MAX_ANNOTATION)
    }

    fun findEmail(psiElement: PsiElement): Map<String,Any?>? {
        return annotationHelper.findAnnMap(psiElement,ValidationClassName.EMAIL_ANNOTATION)
    }
}