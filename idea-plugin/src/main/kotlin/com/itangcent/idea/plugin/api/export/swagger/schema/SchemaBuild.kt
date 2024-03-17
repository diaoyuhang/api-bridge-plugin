package com.itangcent.idea.plugin.api.export.swagger.schema

import io.swagger.v3.oas.models.media.Schema

interface SchemaBuild {
    fun buildSchema(
        requestBody: Any,
        fieldName: String?,
        allObjMap: LinkedHashMap<String, Schema<*>>,
        fieldType: String?
    ): Schema<*>

    fun getType(): Map<String, SchemaBuild>
}