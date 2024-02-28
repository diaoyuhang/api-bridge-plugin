package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.model.Request
import io.swagger.v3.oas.models.media.Schema

interface SchemaBuild {
    fun buildSchema(requestBody:  LinkedHashMap<String, *>, fieldName:String?): Schema<*>

    fun getType(): Map<String, SchemaBuild>
}