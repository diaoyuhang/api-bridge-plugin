package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.model.Request
import com.itangcent.idea.plugin.api.export.swagger.AnnoInfoAssemble
import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.media.Schema

class DoubleSchemaBuild:SchemaBuild {
    override fun buildSchema(
        requestBody: LinkedHashMap<String, *>,
        fieldName: String?,
        allObjMap: LinkedHashMap<String, Schema<*>>
    ): Schema<*> {
        val doubleSchema = PrimitiveType.DOUBLE.createProperty()
        if (fieldName!=null) {
            doubleSchema.name = fieldName
            AnnoInfoAssemble.SchemaAnnoAssemble.assembleInfo(doubleSchema, requestBody, fieldName)
        }
        return doubleSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["double"] = this
        map["java.lang.Double"] = this
        return map
    }
}