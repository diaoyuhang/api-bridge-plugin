package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.model.Request
import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema

class ArraySchemaBuild:SchemaBuild {
    override fun buildSchema(request: Request, fieldName: String): Schema<*> {
        val arraySchema = ArraySchema()
        return arraySchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        TODO("Not yet implemented")
    }
}