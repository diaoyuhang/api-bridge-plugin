package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.constant.Attrs
import com.itangcent.idea.plugin.api.export.swagger.SchemaBuildUtil
import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema

class CustomObjSchemaBuild : SchemaBuild {
    override fun buildSchema(
        requestBody: Any,
        fieldName: String?,
        allObjMap: LinkedHashMap<String, Schema<*>>,
        fieldType: String?
    ): Schema<*> {
        val requestBody = requestBody as LinkedHashMap<String, *>
        if (requestBody.isEmpty()){
            return PrimitiveType.OBJECT.createProperty()
        }
        val schema = Schema<Any>()

        val customObj = allObjMap[fieldType ?: requestBody[Attrs.QUALIFIED_CLASS_NAME_ATTR]]
        if (customObj != null) {
//            schema.`$ref` = Components.COMPONENTS_SCHEMAS_REF + customObj.name
            return customObj
        }

        val objectSchema = ObjectSchema()
        val require = requestBody[Attrs.REQUIRED_ATTR] as LinkedHashMap<String, Boolean>
        objectSchema.required = require.filter { it.value }.map { it.key }
        objectSchema.name = requestBody[Attrs.QUALIFIED_CLASS_NAME_ATTR] as String
        allObjMap[fieldType ?: objectSchema.name] = objectSchema

        val properties = linkedMapOf<String,Schema<*>?>()
        objectSchema.properties= properties
        for((key,_) in requestBody){
            if (key.startsWith(Attrs.PREFIX)){
                continue
            }else{
                val schema = SchemaBuildUtil.obtainTypeSchema(requestBody, key, allObjMap)
                if(schema is ObjectSchema){
//                    val refSchema = Schema<Any>()
//                    refSchema.`$ref` = Components.COMPONENTS_SCHEMAS_REF + schema.name
                    properties[key] = schema
                }else{
                    properties[key] = schema
                }
            }
        }
//        schema.`$ref` = Components.COMPONENTS_SCHEMAS_REF + objectSchema.name
        return objectSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["object"] = this
        return map
    }
}