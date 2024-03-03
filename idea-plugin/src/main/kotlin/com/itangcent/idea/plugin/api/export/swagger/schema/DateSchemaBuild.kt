package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.model.Request
import com.itangcent.idea.plugin.api.export.swagger.AnnoInfoAssemble
import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.media.Schema

class DateSchemaBuild:SchemaBuild {
    override fun buildSchema(
        requestBody: LinkedHashMap<String, *>,
        fieldName: String?,
        allObjMap: LinkedHashMap<String, Schema<*>>
    ): Schema<*> {
        val dateTimeSchema = PrimitiveType.DATE_TIME.createProperty()
        if (fieldName!=null) {
            dateTimeSchema.name = fieldName
            AnnoInfoAssemble.SchemaAnnoAssemble.assembleInfo(dateTimeSchema, requestBody, fieldName)
        }
        return dateTimeSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["java.util.Date"] = this
        return map
    }
}