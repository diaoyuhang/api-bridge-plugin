package com.itangcent.idea.plugin.api.export.swagger.schema

import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.media.Schema

class SchemaObjectBuild:SchemaBuild {
    private val objSchema = PrimitiveType.OBJECT.createProperty()
    override fun buildSchema(
        requestBody: LinkedHashMap<String, *>,
        fieldName: String?,
        allObjMap: LinkedHashMap<String, Schema<*>>
    ): Schema<*> {
        return objSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["*"] = this
        map["java.lang.Object"] = this
        return map
    }
}