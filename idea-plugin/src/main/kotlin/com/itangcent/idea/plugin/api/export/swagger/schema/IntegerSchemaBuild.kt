package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.model.Request
import com.itangcent.idea.plugin.api.export.swagger.AnnoInfoAssemble
import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.Schema

class IntegerSchemaBuild : SchemaBuild {
    override fun buildSchema(
        requestBody: LinkedHashMap<String, *>,
        fieldName: String?,
        allObjMap: LinkedHashMap<String, Schema<*>>
    ): Schema<*> {
        val IntegerSchema = PrimitiveType.INT.createProperty()
        if (fieldName!=null) {
            IntegerSchema.name = fieldName
            AnnoInfoAssemble.SchemaAnnoAssemble.assembleInfo(IntegerSchema, requestBody, fieldName)
        }
        return IntegerSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["java.lang.Integer"] = this
        map["int"] = this
        return map;
    }
}