package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.constant.Attrs
import com.itangcent.common.model.Request
import com.itangcent.idea.plugin.api.export.swagger.AnnoInfoAssemble
import com.itangcent.idea.plugin.api.export.swagger.SchemaBuildUtil
import io.swagger.v3.oas.models.media.MapSchema
import io.swagger.v3.oas.models.media.Schema

class MapSchemaBuild:SchemaBuild {
    override fun buildSchema(
        requestBody: LinkedHashMap<String, *>,
        fieldName: String?,
        allObjMap: LinkedHashMap<String, Schema<*>>
    ): Schema<*> {
        val mapSchema = MapSchema()
        if (fieldName!=null) {
            mapSchema.name = fieldName
            AnnoInfoAssemble.SchemaAnnoAssemble.assembleInfo(mapSchema, requestBody, fieldName)
        }
        val fieldTypeMap = requestBody[Attrs.JAVA_TYPE_ATTR] as LinkedHashMap<String, String>
        val fieldType = fieldTypeMap[fieldName]
        if(fieldType!!.endsWith(Attrs.GT)){
            val firstIndex = fieldType.indexOfFirst { ch -> ch.toString() == Attrs.LT }
            val generics = fieldType.subSequence(firstIndex + 1, fieldType.length - 1)
            val itemType = generics.split(Attrs.COMMA)[1]
            val typeSchemaBuild = SchemaBuildUtil.getTypeSchemaBuild(itemType)

        }
        return mapSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["java.util.Map"] = this
        map["com.google.gson.JsonObject"] = this
        map["com.alibaba.fastjson2.JSONObject"] = this
        return map
    }
}