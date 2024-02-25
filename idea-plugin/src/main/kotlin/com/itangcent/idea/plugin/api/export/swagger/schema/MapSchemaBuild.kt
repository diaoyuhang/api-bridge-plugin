package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.model.Request
import io.swagger.v3.oas.models.media.MapSchema
import io.swagger.v3.oas.models.media.Schema

class MapSchemaBuild:SchemaBuild {
    override fun buildSchema(request: Request, fieldName: String): Schema<*> {
        val mapSchema = MapSchema()
        return mapSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        TODO("Not yet implemented")
    }
}