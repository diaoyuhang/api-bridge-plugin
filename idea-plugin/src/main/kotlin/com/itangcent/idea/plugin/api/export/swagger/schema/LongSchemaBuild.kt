package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.idea.plugin.api.export.swagger.AnnoInfoAssemble
import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.media.Schema

class LongSchemaBuild : SchemaBuild {
    override fun buildSchema(
        requestBody: Any,
        fieldName: String?,
        allObjMap: LinkedHashMap<String, Schema<*>>,
        fieldType: String?
    ): Schema<*> {
        val longSchema = PrimitiveType.LONG.createProperty()
        if (fieldName!=null) {
            longSchema.name = fieldName
            AnnoInfoAssemble.SchemaAnnoAssemble.assembleInfo(longSchema, requestBody as LinkedHashMap<String, *>, fieldName)
        }
        return longSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["java.lang.Long"] = this
        map["long"] = this
        return map;
    }
}