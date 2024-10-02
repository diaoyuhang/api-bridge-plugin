package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.constant.Attrs
import com.itangcent.common.utils.copy
import com.itangcent.idea.plugin.api.export.suv.SuvApiExporter
import com.itangcent.idea.plugin.api.export.swagger.AnnoInfoAssemble
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

        val customObj = allObjMap[fieldType ?: requestBody[Attrs.QUALIFIED_CLASS_NAME_ATTR]]
        if (customObj != null) {
//            schema.`$ref` = Components.COMPONENTS_SCHEMAS_REF + customObj.name
            return if (SuvApiExporter.ApiPlatformExporterAdapter.OBJECT_INTEGRITY_MAP[fieldType] == true){
                customObj.copy() as Schema<*>
            }else{
                ObjectSchema()
            }
        }

        val objectSchema = ObjectSchema()
        val require = requestBody[Attrs.REQUIRED_ATTR] as LinkedHashMap<String, Boolean>
        objectSchema.required = require.filter { it.value }.map { it.key }
        objectSchema.name = requestBody[Attrs.QUALIFIED_CLASS_NAME_ATTR] as String
        val type = fieldType ?: objectSchema.name
        allObjMap[type] = objectSchema

        val properties = linkedMapOf<String,Schema<*>?>()
        objectSchema.properties= properties
        for((key,_) in requestBody){
            if (key.startsWith(Attrs.PREFIX)){
                continue
            }else{
                val schema = SchemaBuildUtil.obtainTypeSchema(requestBody, key, allObjMap)
                AnnoInfoAssemble.SchemaAnnoAssemble.assembleInfo(schema, requestBody, key)
                properties[key] = schema
            }
        }
        SuvApiExporter.ApiPlatformExporterAdapter.OBJECT_INTEGRITY_MAP[type] = true
        return objectSchema
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["object"] = this
        return map
    }
}