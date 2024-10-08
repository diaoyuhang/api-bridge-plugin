package com.itangcent.idea.plugin.api.export.swagger

import com.itangcent.common.constant.Attrs
import com.itangcent.common.model.Request
import com.itangcent.common.utils.getPropertyValue
import com.jetbrains.rd.generator.nova.PredefinedType
import io.swagger.v3.oas.models.media.Schema
import java.math.BigDecimal

class AnnoInfoAssemble {

    object SchemaAnnoAssemble {
        fun assembleInfo(schema: Schema<*>, body: LinkedHashMap<String, *>, fieldName: String) {
            val schemaAnnoInfo = body[Attrs.SCHEMA_ATTR]?.let { it as? LinkedHashMap<String, LinkedHashMap<String, *>> }
            schema.description = schemaAnnoInfo?.get(fieldName)?.get("description") as? String

            val minAnnoInfo = body[Attrs.MIN_ATTR]?.let { it as? LinkedHashMap<String, LinkedHashMap<String, *>> }
            val maxAnnoInfo = body[Attrs.MAX_ATTR]?.let { it as? LinkedHashMap<String, LinkedHashMap<String, *>> }
            val minValue: Int? = minAnnoInfo?.get(fieldName)?.get("value") as? Int
            val maxValue: Int? = maxAnnoInfo?.get(fieldName)?.get("value") as? Int
            if (minValue != null) {
                schema.minimum = BigDecimal(minValue)
            }
            if (maxValue != null) {
                schema.maximum = BigDecimal(maxValue)
            }

            val sizeAnnoInfo = body[Attrs.SIZE_ATTR]?.let { it as? LinkedHashMap<String, LinkedHashMap<String, *>> }
            val minSizeValue: Int? = sizeAnnoInfo?.get(fieldName)?.get("min") as? Int
            val maxSizeValue: Int? = sizeAnnoInfo?.get(fieldName)?.get("max") as? Int
            if (minSizeValue != null) {
                schema.minLength = minSizeValue
            }
            if (maxSizeValue != null) {
                schema.maxLength = maxSizeValue
            }
        }

        fun assembleInfo(schema: Schema<*>, annoInfo: LinkedHashMap<String, *>) {
            val sizeAnnoInfo = annoInfo[Attrs.SIZE_ATTR]?.let { it as? LinkedHashMap<String, Any> }
            val minSizeValue: Int? = sizeAnnoInfo?.get("min") as? Int
            val maxSizeValue: Int? = sizeAnnoInfo?.get("max") as? Int

            if (minSizeValue != null) {
                schema.minLength = minSizeValue
            }
            if (maxSizeValue != null) {
                schema.maxLength = maxSizeValue
            }

            val minAnnoInfo = annoInfo[Attrs.MIN_ATTR]?.let { it as? LinkedHashMap<String, Any> }
            val maxAnnoInfo = annoInfo[Attrs.MAX_ATTR]?.let { it as? LinkedHashMap<String, Any> }

            val minValue: Int? = minAnnoInfo?.get("value") as? Int
            val maxValue: Int? = maxAnnoInfo?.get("value") as? Int
            if (minValue != null) {
                schema.minimum = BigDecimal(minValue)
            }
            if (maxValue != null) {
                schema.maximum = BigDecimal(maxValue)
            }

            val schemaAnnoInfo = annoInfo[Attrs.SCHEMA_ATTR]?.let { it as? LinkedHashMap<String, Any> }
            val schemaDescription = schemaAnnoInfo?.get("description") as? String
            if (schemaDescription != null) {
                schema.description = schemaDescription
            }
        }

    }
}