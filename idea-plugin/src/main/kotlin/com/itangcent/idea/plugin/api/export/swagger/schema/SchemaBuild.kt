package com.itangcent.idea.plugin.api.export.swagger.schema

import io.swagger.v3.oas.models.media.Schema

interface SchemaBuild {
    fun buildSchema(requestBody:  LinkedHashMap<String, *>, fieldName:String?,allObjMap:LinkedHashMap<String,Schema<*>>): Schema<*>

    fun getType(): Map<String, SchemaBuild>
}