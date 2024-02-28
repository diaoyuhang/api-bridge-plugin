package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.model.Request
import com.itangcent.idea.plugin.api.export.swagger.AnnoInfoAssemble
import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.media.Schema

class BooleanSchemaBuild:SchemaBuild {
    override fun buildSchema(requestBody:  LinkedHashMap<String, *>, fieldName: String?): Schema<*> {
        val booleanSchema = PrimitiveType.BOOLEAN.createProperty()
        if (fieldName != null) {
            booleanSchema.name = fieldName
            AnnoInfoAssemble.SchemaAnnoAssemble.assembleInfo(booleanSchema, requestBody, fieldName)
        }
        return booleanSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["boolean"] = this
        map["Boolean"] = this
        return map
    }
}