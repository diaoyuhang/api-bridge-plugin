package com.itangcent.idea.plugin.api.export.swagger

import com.itangcent.common.constant.Attrs
import com.itangcent.common.model.Request
import com.itangcent.idea.plugin.api.export.swagger.schema.SchemaBuild
import com.itangcent.idea.plugin.api.export.swagger.schema.StringSchemaBuild
import io.swagger.v3.oas.models.media.Schema

object SchemaBuildUtil {
    private val typeSchemaBuildMap = mutableMapOf<String, SchemaBuild>()

    init {
        val stringSchemaBuild = StringSchemaBuild()
        typeSchemaBuildMap.putAll(stringSchemaBuild.getType())


    }

    fun obtainTypeSchema(request: Request, fieldName: String): Schema<*>? {
        val body: LinkedHashMap<String, *>? = request.body?.let { it as? LinkedHashMap<String, *> }
        if (body == null) {
            return null
        }
        val fieldTypeMap = body[Attrs.JAVA_TYPE_ATTR]?.let { it as LinkedHashMap<String, String> } ?: linkedMapOf()
        val fieldType = fieldTypeMap[fieldName] as String
        val schemaBuild = typeSchemaBuildMap[fieldType]
        return schemaBuild!!.buildSchema(request, fieldName)
    }

}