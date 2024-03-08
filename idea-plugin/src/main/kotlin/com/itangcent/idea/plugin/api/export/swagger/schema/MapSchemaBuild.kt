package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.model.Request
import com.itangcent.idea.plugin.api.export.swagger.AnnoInfoAssemble
import io.swagger.v3.oas.models.media.MapSchema
import io.swagger.v3.oas.models.media.Schema

class MapSchemaBuild:SchemaBuild {
    override fun buildSchema(
        requestBody: LinkedHashMap<String, *>,
        fieldName: String?,
        allObjMap: LinkedHashMap<String, Schema<*>>
    ): Schema<*> {
        val mapSchema = MapSchema()
        if (fieldName!=null) {
            mapSchema.name = fieldName
            AnnoInfoAssemble.SchemaAnnoAssemble.assembleInfo(mapSchema, requestBody, fieldName)
        }
        return mapSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["java.util.Map"] = this
        map["java.lang.Object"] = this
        return map
    }
}