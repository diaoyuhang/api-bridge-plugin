package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.model.Request
import com.itangcent.idea.plugin.api.export.swagger.AnnoInfoAssemble
import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.media.Schema

class ByteSchemaBuild:SchemaBuild {
    override fun buildSchema(requestBody:  LinkedHashMap<String, *>, fieldName: String?): Schema<*> {
        val byteSchema = PrimitiveType.BYTE.createProperty()
        if (fieldName!=null) {
            byteSchema.name = fieldName
            AnnoInfoAssemble.SchemaAnnoAssemble.assembleInfo(byteSchema, requestBody, fieldName)
        }
        return byteSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["byte"] = this
        map["Byte"] = this
        return map
    }
}