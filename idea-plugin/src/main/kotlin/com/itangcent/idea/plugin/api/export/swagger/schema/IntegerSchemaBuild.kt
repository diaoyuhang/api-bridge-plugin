package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.idea.plugin.api.export.swagger.AnnoInfoAssemble
import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.media.Schema

class IntegerSchemaBuild : SchemaBuild {
    override fun buildSchema(
        requestBody: LinkedHashMap<String, *>,
        fieldName: String?,
        allObjMap: LinkedHashMap<String, Schema<*>>
    ): Schema<*> {
        val integerSchema = PrimitiveType.INT.createProperty()
        if (fieldName!=null) {
            integerSchema.name = fieldName
            AnnoInfoAssemble.SchemaAnnoAssemble.assembleInfo(integerSchema, requestBody, fieldName)
        }
        return integerSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["java.lang.Integer"] = this
        map["int"] = this
        return map;
    }
}