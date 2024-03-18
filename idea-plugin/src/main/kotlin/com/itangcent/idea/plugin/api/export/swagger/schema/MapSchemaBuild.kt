package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.constant.Attrs
import com.itangcent.idea.plugin.api.export.swagger.AnnoInfoAssemble
import com.itangcent.idea.plugin.api.export.swagger.SchemaBuildUtil
import io.swagger.v3.oas.models.media.MapSchema
import io.swagger.v3.oas.models.media.Schema
import org.apache.commons.lang3.StringUtils

class MapSchemaBuild:SchemaBuild {
    override fun buildSchema(
        requestBody: Any,
        fieldName: String?,
        allObjMap: LinkedHashMap<String, Schema<*>>,
        fieldType: String?
    ): Schema<*> {
        val mapSchema = MapSchema()
        val requestBody = requestBody as LinkedHashMap<String, *>
        if (fieldName!=null) {
            mapSchema.name = fieldName
            AnnoInfoAssemble.SchemaAnnoAssemble.assembleInfo(mapSchema, requestBody, fieldName)
        }
        val fieldTypeMap = requestBody[Attrs.JAVA_TYPE_ATTR] as LinkedHashMap<String, String>
        val fieldType = fieldTypeMap[fieldName]

        mapSchema.additionalProperties = assembleSchema(fieldType!!, requestBody, fieldName, allObjMap)

        return mapSchema
    }

    private fun assembleSchema(
        fieldType: String,
        requestBody: LinkedHashMap<String, *>,
        fieldName: String?,
        allObjMap: LinkedHashMap<String, Schema<*>>
    ):Schema<*> {
        if (fieldType.endsWith(Attrs.GT)) {
            val firstIndex = fieldType.indexOfFirst { ch -> ch.toString() == Attrs.LT }
            val generics = fieldType.subSequence(firstIndex + 1, fieldType.length - 1)
            val itemType = generics.split(Attrs.COMMA)[1]
            val paramBody = (requestBody[fieldName] as LinkedHashMap<String,*>)[StringUtils.EMPTY]
            return SchemaBuildUtil.getTypeSchemaBuild(itemType).buildSchema(paramBody!!,null,allObjMap,itemType)

        } else {

            return SchemaBuildUtil.getTypeSchemaBuild("*").buildSchema(requestBody, null, allObjMap, fieldType)
        }
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["java.util.Map"] = this
        map["java.util.HashMap"] = this
        map["com.google.gson.JsonObject"] = this
        map["com.alibaba.fastjson2.JSONObject"] = this
        return map
    }
}