package com.itangcent.idea.plugin.api.export.swagger

import com.intellij.xdebugger.impl.breakpoints.getType
import com.itangcent.common.constant.Attrs
import com.itangcent.common.model.Request
import com.itangcent.idea.plugin.api.export.swagger.schema.*
import com.itangcent.idea.plugin.api.export.swagger.schema.CustomObjSchemaBuild
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema
import org.apache.commons.lang3.StringUtils

object SchemaBuildUtil {
    private val typeSchemaBuildMap = mutableMapOf<String, SchemaBuild>()

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
        typeSchemaBuildMap.putAll(ObjectSchemaBuild().getType())

    }

    fun obtainTypeSchema(requestBody: Any?, allObjMap: LinkedHashMap<String, Schema<*>>): Schema<*>? {
        if (requestBody == null) {
            return null
        }

        if (requestBody is LinkedHashMap<*, *>) {
            val body = requestBody as LinkedHashMap<String, *>
            val schemaBuild = if(body.contains(StringUtils.EMPTY)){
                getTypeSchemaBuild("java.util.Map")
            }else{
                 getTypeSchemaBuild(requestBody[Attrs.QUALIFIED_CLASS_NAME_ATTR] as String)
            }

            return schemaBuild.buildSchema(body, null, allObjMap, null)
        } else if (requestBody is ArrayList<*>) {
            val arraySchemaBuild = getTypeSchemaBuild("[]")
            return arraySchemaBuild.buildSchema(requestBody, null, allObjMap, null)
        }else{
            return getTypeSchemaBuild(requestBody.javaClass.name).buildSchema(requestBody, null, allObjMap, null)
        }
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
        var body = requestBody
        if(schemaBuild is CustomObjSchemaBuild){
            body = requestBody[fieldName] as LinkedHashMap<String, *>
        }

        return schemaBuild.buildSchema(body, fieldName, allObjMap, fieldType)
    }

    fun getTypeSchemaBuild(fieldType: String): SchemaBuild {
        return typeSchemaBuildMap[fieldType]
            ?: run {
                if (fieldType.endsWith(Attrs.ARRAY_TYPE_SUFFIX)||fieldType.startsWith("java.util.List")) {
                    typeSchemaBuildMap[Attrs.ARRAY_TYPE_SUFFIX]
                } else if (fieldType.startsWith("java.util.Map")||
                    fieldType.startsWith("java.util.HashMap")||
                    fieldType.startsWith("com.google.gson.JsonObject")||
                    fieldType.startsWith("com.alibaba.fastjson2.JSONObject")){
                    typeSchemaBuildMap["java.util.Map"]
                } else {
                    null
                }
            }
            ?: CustomObjSchemaBuild()
    }
}