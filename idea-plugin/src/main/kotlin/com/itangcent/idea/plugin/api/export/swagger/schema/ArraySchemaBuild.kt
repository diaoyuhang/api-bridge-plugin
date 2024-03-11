package com.itangcent.idea.plugin.api.export.swagger.schema

import com.itangcent.common.constant.Attrs
import com.itangcent.idea.plugin.api.export.swagger.SchemaBuildUtil
import com.jetbrains.rd.generator.nova.PredefinedType
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema

class ArraySchemaBuild : SchemaBuild {

    val LIST_STRING = "java.util.List"
    override fun buildSchema(
        requestBody: LinkedHashMap<String, *>,
        fieldName: String?,
        allObjMap: LinkedHashMap<String, Schema<*>>
    ): Schema<*> {
        val fieldTypeMap = requestBody[Attrs.JAVA_TYPE_ATTR] as LinkedHashMap<String, String>
        val fieldType = fieldTypeMap[fieldName]
        val arraySchema = assembleSchema(fieldType!!, requestBody, fieldName, allObjMap) {
            var paramBody = requestBody[fieldName]
            while (paramBody is ArrayList<*>) {
                paramBody = paramBody[0]
            }
            paramBody as LinkedHashMap<String, *>
        }

        return arraySchema
    }

    private fun assembleSchema(
        fieldType: String,
        requestBody: LinkedHashMap<String, *>,
        fieldName: String?,
        allObjMap: LinkedHashMap<String, Schema<*>>,
        handle : ()-> LinkedHashMap<String, *>
    ): ArraySchema {
        val arraySchema = ArraySchema()
        arraySchema.name = fieldName

        if (fieldType.endsWith(SchemaBuildUtil.ARRAY_TYPE_SUFFIX)) {
            val itemType = fieldType.subSequence(0, fieldType.length - 2)
            doAssemble(itemType, arraySchema, requestBody, allObjMap,handle)
        } else if (fieldType.endsWith(Attrs.GT)) {
            val firstIndex = fieldType.indexOfFirst { ch -> ch.toString() == Attrs.LT }
            var itemType = fieldType.subSequence(firstIndex + 1, fieldType.length - 1).toString()
            doAssemble(itemType, arraySchema, requestBody, allObjMap,handle)
        }

        return arraySchema
    }

    private fun doAssemble(
        itemType: CharSequence,
        arraySchema: ArraySchema,
        requestBody: LinkedHashMap<String, *>,
        allObjMap: LinkedHashMap<String, Schema<*>>,
        handle : ()-> LinkedHashMap<String, *>
    ) {
        if (itemType.endsWith(SchemaBuildUtil.ARRAY_TYPE_SUFFIX)) {
            arraySchema.items = assembleSchema(itemType.toString(), requestBody, null, allObjMap,handle)
        } else if (itemType.startsWith(LIST_STRING)) {
            arraySchema.items = assembleSchema(itemType.toString(), requestBody, null, allObjMap,handle)
        } else {
            val typeSchemaBuild = SchemaBuildUtil.getTypeSchemaBuild(itemType.toString())
            if (typeSchemaBuild is ObjectSchemaBuild) {
                val paramBody = handle()
                arraySchema.items = typeSchemaBuild.buildSchema(paramBody, null, allObjMap)
            } else {
                arraySchema.items = typeSchemaBuild.buildSchema(requestBody, null, allObjMap)
            }
        }
    }

    override fun getType(): Map<String, SchemaBuild> {
        val map = mutableMapOf<String, SchemaBuild>()
        map["[]"] = this
        map["java.util.List"] = this
        return map
    }
}