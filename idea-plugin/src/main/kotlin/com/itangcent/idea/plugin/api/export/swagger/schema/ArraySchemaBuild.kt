package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.constant.Attrs
import com.itangcent.common.model.Request
import com.itangcent.idea.plugin.api.export.swagger.SchemaBuildUtil
import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema

class ArraySchemaBuild:SchemaBuild {
    override fun buildSchema(requestBody:  LinkedHashMap<String, *>, fieldName: String?): Schema<*> {
        val arraySchema = ArraySchema()
        arraySchema.name = fieldName
        val fieldTypeMap = requestBody[Attrs.JAVA_TYPE_ATTR] as LinkedHashMap<String, String>
        val fieldType = fieldTypeMap[fieldName]
        if (fieldType!!.endsWith(SchemaBuildUtil.ARRAY_TYPE_SUFFIX)){
            val itemType = fieldType.subSequence(0, fieldType.length - 2)
            val typeSchemaBuild = SchemaBuildUtil.getTypeSchemaBuild(itemType.toString())

            if (typeSchemaBuild is ObjectSchemaBuild){

                val objectSchema = typeSchemaBuild.buildSchema(
                    (requestBody[fieldName] as List<*>)[0] as LinkedHashMap<String, *>, null)
                arraySchema.items = objectSchema
            }else{
                arraySchema.items = typeSchemaBuild.buildSchema(requestBody,null)
            }
        }else{

        }
        return arraySchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["[]"] = this
        map["List"] = this
        return map
    }
}