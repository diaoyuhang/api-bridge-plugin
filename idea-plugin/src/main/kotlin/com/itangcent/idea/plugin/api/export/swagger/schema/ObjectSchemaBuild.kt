package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.model.Request
import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.media.Schema

class ObjectSchemaBuild : SchemaBuild {
    override fun buildSchema(requestBody:  LinkedHashMap<String, *>, fieldName: String?): Schema<*> {
        val objectSchema = PrimitiveType.OBJECT.createProperty()
        return objectSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        return map
    }
}