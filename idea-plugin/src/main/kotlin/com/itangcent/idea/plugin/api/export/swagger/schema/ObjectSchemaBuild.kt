package com.itangcent.idea.plugin.api.export.swagger.schema

import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.media.Schema

class ObjectSchemaBuild:SchemaBuild {
    private val objSchema = PrimitiveType.OBJECT.createProperty()
    override fun buildSchema(
        requestBody: Any,
        fieldName: String?,
        allObjMap: LinkedHashMap<String, Schema<*>>,
        fieldType: String?
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