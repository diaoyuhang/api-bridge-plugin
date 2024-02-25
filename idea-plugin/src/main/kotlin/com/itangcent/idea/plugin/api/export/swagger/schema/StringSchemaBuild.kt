package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.model.Request
import com.itangcent.idea.plugin.api.export.swagger.AnnoInfoAssemble
import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.media.Schema

class StringSchemaBuild : SchemaBuild {
    override fun buildSchema(request: Request, fieldName: String): Schema<*> {
        val stringSchema = PrimitiveType.STRING.createProperty()
        stringSchema.name = fieldName
        AnnoInfoAssemble.SchemaAnnoAssemble.assembleInfo(stringSchema, request, fieldName)
        return stringSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["String"] = this
        return map
    }
}