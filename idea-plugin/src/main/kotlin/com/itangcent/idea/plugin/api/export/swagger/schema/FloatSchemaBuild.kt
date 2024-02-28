package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.model.Request
import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.media.Schema

class FloatSchemaBuild:SchemaBuild {
    override fun buildSchema(requestBody:  LinkedHashMap<String, *>, fieldName: String?): Schema<*> {
        val floatSchema = PrimitiveType.FLOAT.createProperty()

        return floatSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["float"] = this
        map["Float"] = this
        return map
    }
}