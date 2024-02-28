package com.itangcent.idea.plugin.api.export.swagger

import com.itangcent.common.constant.Attrs
import com.itangcent.common.model.Request
import com.itangcent.common.utils.getPropertyValue
import com.jetbrains.rd.generator.nova.PredefinedType
import io.swagger.v3.oas.models.media.Schema
import java.math.BigDecimal

class AnnoInfoAssemble {

    object SchemaAnnoAssemble {
        fun assembleInfo(schema: Schema<*>, body:  LinkedHashMap<String, *>, fieldName:String) {
            val schemaAnnoInfo = body.get(Attrs.SCHEMA_ATTR)?.let { it as? LinkedHashMap<String, *> }
            schema.description = schemaAnnoInfo?.get(fieldName)?.getPropertyValue("description") as? String

            val minAnnoInfo = body[Attrs.MIN_ATTR]?.let { it as? LinkedHashMap<String, *> }
            val maxAnnoInfo = body[Attrs.MAX_ATTR]?.let { it as? LinkedHashMap<String, *> }
            val minValue: Int? = minAnnoInfo?.get(fieldName)?.getPropertyValue("value") as? Int
            val maxValue: Int? = maxAnnoInfo?.get(fieldName)?.getPropertyValue("value") as? Int
            if (minValue != null) {
                schema.minimum = BigDecimal(minValue)
            }
            if (maxValue != null) {
                schema.maximum = BigDecimal(maxValue)
            }

            val sizeAnnoInfo = body[Attrs.SIZE_ATTR]?.let { it as? LinkedHashMap<String, *> }
            val minSizeValue: Int? = sizeAnnoInfo?.get(fieldName)?.getPropertyValue("min") as? Int
            val maxSizeValue: Int? = sizeAnnoInfo?.get(fieldName)?.getPropertyValue("max") as? Int
            if (minSizeValue != null) {
                schema.minLength = minSizeValue
            }
            if (maxSizeValue != null) {
                schema.maxLength = maxSizeValue
            }
        }

    }
}