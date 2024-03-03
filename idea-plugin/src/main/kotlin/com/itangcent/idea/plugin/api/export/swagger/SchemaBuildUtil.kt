package com.itangcent.idea.plugin.api.export.swagger

import com.itangcent.common.constant.Attrs
import com.itangcent.common.model.Request
import com.itangcent.idea.plugin.api.export.swagger.schema.*
import com.itangcent.idea.plugin.api.export.swagger.schema.ObjectSchemaBuild
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema

object SchemaBuildUtil {
    private val typeSchemaBuildMap = mutableMapOf<String, SchemaBuild>()
    const val ARRAY_TYPE_SUFFIX = "[]"

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

    fun obtainTypeSchema(request: Request, fieldName: String, allObjMap: LinkedHashMap<String, Schema<*>>): Schema<*>? {
        if (request.body == null) {
            return null
        }
        var body: LinkedHashMap<String, *> = linkedMapOf<String, Any?>()
        if (request.body is LinkedHashMap<*, *>) {
            body = request.body as LinkedHashMap<String, *>
        } else if (request.body is ArrayList<*>) {
            val arraySchema = ArraySchema()
            val objectSchemaBuild = getTypeSchemaBuild("object")
            val linkedHashMap = (request.body as ArrayList<*>)[0] as LinkedHashMap<String, *>
            arraySchema.items = objectSchemaBuild.buildSchema(linkedHashMap, null, allObjMap)
            return arraySchema
        }
        val fieldTypeMap = body[Attrs.JAVA_TYPE_ATTR]?.let { it as LinkedHashMap<String, String> } ?: linkedMapOf()
        val fieldType = fieldTypeMap[fieldName] as String
        val schemaBuild = getTypeSchemaBuild(fieldType)
        return schemaBuild.buildSchema(body, fieldName, allObjMap)
    }

    fun obtainTypeSchema(
        requestBody: LinkedHashMap<String, *>,
        fieldName: String,
        allObjMap: LinkedHashMap<String, Schema<*>>
    ): Schema<*> {
        val fieldTypeMap =
            requestBody[Attrs.JAVA_TYPE_ATTR]?.let { it as LinkedHashMap<String, String> } ?: linkedMapOf()
        val fieldType = fieldTypeMap[fieldName] as String
        val schemaBuild = getTypeSchemaBuild(fieldType)
        return schemaBuild.buildSchema(requestBody, fieldName, allObjMap)
    }

    fun getTypeSchemaBuild(fieldType: String): SchemaBuild {
        return typeSchemaBuildMap[fieldType]
            ?: run {
                if (fieldType.endsWith(ARRAY_TYPE_SUFFIX)) {
                    typeSchemaBuildMap[ARRAY_TYPE_SUFFIX]
                } else {
                    null
                }
            }
            ?: ObjectSchemaBuild()
    }
}