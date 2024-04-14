package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.constant.Attrs
import com.itangcent.idea.plugin.api.export.swagger.AnnoInfoAssemble
import com.itangcent.idea.plugin.api.export.swagger.SchemaBuildUtil
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema

class ArraySchemaBuild : SchemaBuild {

    override fun buildSchema(
        requestBody: Any,
        fieldName: String?,
        allObjMap: LinkedHashMap<String, Schema<*>>,
        fieldType: String?
    ): Schema<*> {
        val arraySchema = ArraySchema()
        if(fieldName!=null) {
            arraySchema.name = fieldName
        }
        val body = if (requestBody is LinkedHashMap<*, *>) {
            (requestBody as LinkedHashMap<String, *>)[fieldName] as List<Any>
        } else {
            requestBody as List<Any>
        }

        arraySchema.items = if (fieldType != null) {
             assembleSchema(fieldType, body, allObjMap)
        }else{
             SchemaBuildUtil.obtainTypeSchema(body[0],allObjMap)
        }
        return arraySchema
    }

    private fun assembleSchema(
        fieldType: String,
        requestBody: List<Any>,
        allObjMap: LinkedHashMap<String, Schema<*>>
    ): Schema<*> {

        if (fieldType.endsWith(Attrs.ARRAY_TYPE_SUFFIX)) {
            val itemType = fieldType.subSequence(0, fieldType.length - 2).toString()
            return SchemaBuildUtil.getTypeSchemaBuild(itemType).buildSchema(requestBody[0],null,allObjMap,itemType)
        } else if (fieldType.endsWith(Attrs.GT)) {
            val firstIndex = fieldType.indexOfFirst { ch -> ch.toString() == Attrs.LT }
            var itemType = fieldType.subSequence(firstIndex + 1, fieldType.length - 1).toString()

            val commonIndex = itemType.indexOfFirst { ch->ch.toString()==Attrs.COMMA }
            if(commonIndex>-1){
                val ltIndex = itemType.indexOfFirst { ch -> ch.toString() == Attrs.LT }
                if (ltIndex>commonIndex){
                    itemType = itemType.split(Attrs.COMMA)[1]
                    return SchemaBuildUtil.getTypeSchemaBuild(itemType).buildSchema(requestBody,null,allObjMap,itemType)
                }
            }
            return SchemaBuildUtil.getTypeSchemaBuild(itemType).buildSchema(requestBody[0],null,allObjMap,itemType)
        }else{
            return SchemaBuildUtil.getTypeSchemaBuild("*").buildSchema(requestBody[0],null,allObjMap,null)
        }
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["[]"] = this
        map["java.util.List"] = this
        return map
    }
}