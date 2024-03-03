package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.constant.Attrs
import com.itangcent.common.model.Request
import com.itangcent.idea.plugin.api.export.swagger.SchemaBuildUtil
import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.media.Schema

class ObjectSchemaBuild : SchemaBuild {
    override fun buildSchema(
        requestBody: LinkedHashMap<String, *>,
        fieldName: String?,
        allObjMap: LinkedHashMap<String, Schema<*>>
    ): Schema<*> {
        val objectSchema = PrimitiveType.OBJECT.createProperty()
        val require = requestBody[Attrs.REQUIRED_ATTR] as LinkedHashMap<String, Boolean>
        objectSchema.required = require.filter { it.value }.map { it.key }
        objectSchema.name = requestBody[Attrs.QUALIFIED_CLASS_NAME_ATTR] as String
        allObjMap[objectSchema.name] = objectSchema

        val properties = linkedMapOf<String,Schema<*>?>()
        objectSchema.properties= properties
        for((key,value) in requestBody){
            if (key.startsWith(Attrs.PREFIX)){
                continue
            }else{
                properties[key] = SchemaBuildUtil.obtainTypeSchema(requestBody, key, allObjMap)
            }
        }
        return objectSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["object"] = this
        return map
    }
}