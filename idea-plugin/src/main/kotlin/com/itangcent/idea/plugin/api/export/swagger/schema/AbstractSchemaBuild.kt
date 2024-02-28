package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.model.Request
import io.swagger.v3.oas.models.media.Schema

abstract class AbsSchemaBuild : SchemaBuild {
    fun createSchema(request: Request, fieldName: String): Schema<*> {
        val schema = buildSchema(request, fieldName)
        additionalSchemaAnnoInfo(schema,request,fieldName)
        return schema
    }
    fun additionalSchemaAnnoInfo(schema:Schema<*>,request: Request, fieldName:String){
        schema.description
    }
}