package com.itangcent.idea.plugin.api.export.swagger

import com.itangcent.common.constant.Attrs
import com.itangcent.common.model.Request
import com.itangcent.idea.plugin.api.export.swagger.schema.*
import io.swagger.v3.oas.models.media.Schema

object SchemaBuildUtil {
    private val typeSchemaBuildMap = mutableMapOf<String, SchemaBuild>()
    private const val ARRAY_TYPE_SUFFIX = "[]"

    init {
        typeSchemaBuildMap.putAll(StringSchemaBuild().getType())
        typeSchemaBuildMap.putAll(BooleanSchemaBuild().getType())
        typeSchemaBuildMap.putAll(ByteSchemaBuild().getType())
        typeSchemaBuildMap.putAll(DateSchemaBuild().getType())
        typeSchemaBuildMap.putAll(DoubleSchemaBuild().getType())
        typeSchemaBuildMap.putAll(FloatSchemaBuild().getType())
        typeSchemaBuildMap.putAll(IntegerSchemaBuild().getType())
        typeSchemaBuildMap.putAll(LongSchemaBuild().getType())
        typeSchemaBuildMap.putAll(ArraySchemaBuild().getType())
        typeSchemaBuildMap.putAll(MapSchemaBuild().getType())

    }

    fun obtainTypeSchema(request: Request, fieldName: String): Schema<*>? {
        val body: LinkedHashMap<String, *>? = request.body?.let { it as? LinkedHashMap<String, *> }
        if (body == null) {
            return null
        }
        val fieldTypeMap = body[Attrs.JAVA_TYPE_ATTR]?.let { it as LinkedHashMap<String, String> } ?: linkedMapOf()
        val fieldType = fieldTypeMap[fieldName] as String
        val schemaBuild = getTypeSchemaBuild(fieldType)
        return schemaBuild!!.buildSchema(request, fieldName)
    }

    private fun getTypeSchemaBuild(fieldType: String): SchemaBuild {
        return typeSchemaBuildMap[fieldType]
            ?: run {
                if (fieldType.endsWith(ARRAY_TYPE_SUFFIX)) {
                    typeSchemaBuildMap[ARRAY_TYPE_SUFFIX]
                } else {
                    null
                }
            }
            ?: throw RuntimeException("${fieldType} not found TypeSchemaBuild")
    }
}