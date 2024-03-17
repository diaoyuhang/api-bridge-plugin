package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.idea.plugin.api.export.swagger.AnnoInfoAssemble
import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.media.Schema

class StringSchemaBuild : SchemaBuild {
    override fun buildSchema(
        requestBody: Any,
        fieldName: String?,
        allObjMap: LinkedHashMap<String, Schema<*>>,
        fieldType: String?
    ): Schema<*> {
        val stringSchema = PrimitiveType.STRING.createProperty()
        if (fieldName!=null) {
            stringSchema.name = fieldName
            AnnoInfoAssemble.SchemaAnnoAssemble.assembleInfo(stringSchema, requestBody as LinkedHashMap<String, *>, fieldName)
        }
        return stringSchema
    }
    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["java.lang.String"] = this
        return map
    }

}