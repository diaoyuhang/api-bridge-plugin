package com.itangcent.idea.plugin.api.export.swagger

import com.itangcent.common.constant.Attrs
import com.itangcent.common.spi.SpiUtils
import com.itangcent.idea.plugin.api.export.swagger.schema.*
import com.itangcent.intellij.logger.Logger
import io.swagger.v3.oas.models.media.MapSchema
import io.swagger.v3.oas.models.media.Schema
import org.apache.commons.lang3.StringUtils

object SchemaBuildUtil {
    private val typeSchemaBuildMap = mutableMapOf<String, SchemaBuild>()
    var logger: Logger? = SpiUtils.loadService(Logger::class)
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
            var body = requestBody as LinkedHashMap<String, *>
            if (body.contains(StringUtils.EMPTY)) {
                val mapSchema = MapSchema()
                val bodyValue = body[StringUtils.EMPTY] ?: Object()

                val type: String =
                    if (bodyValue is LinkedHashMap<*, *> && bodyValue.containsKey(Attrs.QUALIFIED_CLASS_NAME_ATTR)) {
                        bodyValue[Attrs.QUALIFIED_CLASS_NAME_ATTR].toString()
                    } else {
                        bodyValue::class.java.name
                    }

                mapSchema.additionalProperties = obtainTypeSchema(bodyValue, allObjMap)
                return mapSchema
            } else {
                val type = requestBody[Attrs.QUALIFIED_CLASS_NAME_ATTR] ?: requestBody::class.java.name
                val schemaBuild = getTypeSchemaBuild(type as String)
                return schemaBuild.buildSchema(body, null, allObjMap, null)
            }
        } else if (requestBody is ArrayList<*>) {
            val arraySchemaBuild = getTypeSchemaBuild("[]")
            return arraySchemaBuild.buildSchema(requestBody, null, allObjMap, null)
        } else {
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
                if (fieldType.endsWith(Attrs.ARRAY_TYPE_SUFFIX) || fieldType.startsWith("java.util.List")) {
                    typeSchemaBuildMap[Attrs.ARRAY_TYPE_SUFFIX]
                } else if (fieldType.startsWith("java.util.Map") ||
                    fieldType.startsWith("java.util.HashMap")
                ) {
                    typeSchemaBuildMap["java.util.Map"]
                } else if (fieldType.endsWith(".JSONObject",true)){
                    typeSchemaBuildMap["*"]
                }else {
                    val firstIndex = fieldType.indexOfFirst { ch -> ch.toString() == Attrs.LT }
                    val className = if (firstIndex > -1) {
                        fieldType.subSequence(0, firstIndex).toString()
                    } else {
                        fieldType
                    }

                    try {
                        val clazz = Class.forName(className)
                        if (Collection::class.java.isAssignableFrom(clazz)) {
                            typeSchemaBuildMap["java.util.List"]
                        } else if (Map::class.java.isAssignableFrom(clazz)) {
                            typeSchemaBuildMap["java.util.Map"]
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        logger?.error("error getTypeSchemaBuild:"+e.message)
                        null
                    } finally {
                        null
                    }
                }
            }
            ?: CustomObjSchemaBuild()
    }
}