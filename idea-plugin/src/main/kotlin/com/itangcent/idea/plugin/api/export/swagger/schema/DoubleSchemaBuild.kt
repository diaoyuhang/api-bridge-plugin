package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.model.Request
import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.media.Schema

class DoubleSchemaBuild:SchemaBuild {
    override fun buildSchema(request: Request, fieldName: String): Schema<*> {
        val doubleSchema = PrimitiveType.DOUBLE.createProperty()
        return doubleSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["double"] = this
        map["Double"] = this
        return map
    }
}