package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.model.Request
import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.media.Schema

class LongSchemaBuild : SchemaBuild {
    override fun buildSchema(request: Request, fieldName: String): Schema<*> {
        val longSchema = PrimitiveType.LONG.createProperty()
        return longSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["Long"] = this
        map["long"] = this
        return map;
    }
}