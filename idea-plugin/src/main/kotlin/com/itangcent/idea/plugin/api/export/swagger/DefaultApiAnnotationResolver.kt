package com.itangcent.idea.plugin.api.export.swagger

import com.google.inject.Inject
import com.google.inject.Singleton
import com.intellij.psi.PsiElement
import com.itangcent.intellij.jvm.AnnotationHelper


@Singleton
class DefaultApiAnnotationResolver {

    @Inject
    private lateinit var annotationHelper: AnnotationHelper
    fun findTag(psiElement: PsiElement): Map<String, Any?>? {
        return annotationHelper.findAnnMap(psiElement,OpenApiClassName.TAG_ANNOTATION)
    }

    fun findOperation(psiElement: PsiElement): Map<String,Any?>? {
        return annotationHelper.findAnnMap(psiElement,OpenApiClassName.OPERATION_ANNOTATION)
    }

    fun findApiResponses(psiElement: PsiElement): Map<String,Any?>? {
        return annotationHelper.findAnnMap(psiElement,OpenApiClassName.API_RESPONSES_ANNOTATION)
    }

    fun findApiResponse(psiElement: PsiElement): Map<String,Any?>? {
        return annotationHelper.findAnnMap(psiElement,OpenApiClassName.API_RESPONSE_ANNOTATION)
    }

    fun findSchema(psiElement: PsiElement): Map<String,Any?>? {
        return annotationHelper.findAnnMap(psiElement,OpenApiClassName.SCHEMA_ANNOTATION)
    }

    fun findMin(psiElement: PsiElement): Map<String,Any?>? {
        return annotationHelper.findAnnMap(psiElement,OpenApiClassName.MIN_ANNOTATION)
    }

    fun findSize(psiElement: PsiElement): Map<String,Any?>? {
        return annotationHelper.findAnnMap(psiElement,OpenApiClassName.SIZE_ANNOTATION)
    }

    fun findMax(psiElement: PsiElement): Map<String,Any?>? {
        return annotationHelper.findAnnMap(psiElement,OpenApiClassName.MAX_ANNOTATION)
    }

    fun findEmail(psiElement: PsiElement): Map<String,Any?>? {
        return annotationHelper.findAnnMap(psiElement,OpenApiClassName.EMAIL_ANNOTATION)
    }

    fun findDeprecated(psiElement: PsiElement): Map<String,Any?>? {
        return annotationHelper.findAnnMap(psiElement,OpenApiClassName.DEPRECATED_ANNOTATION)
    }
}