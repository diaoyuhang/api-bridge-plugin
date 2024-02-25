package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.model.Request
import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.Schema

class IntegerSchemaBuild : SchemaBuild {
    override fun buildSchema(request: Request, fieldName: String): Schema<*> {
        val IntegerSchema = PrimitiveType.INT.createProperty()
        return IntegerSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["Integer"] = this
        map["int"] = this
        return map;
    }
}