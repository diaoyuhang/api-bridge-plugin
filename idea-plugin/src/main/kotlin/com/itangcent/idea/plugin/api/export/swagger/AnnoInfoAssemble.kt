package com.itangcent.idea.plugin.api.export.swagger

import com.itangcent.common.constant.Attrs
import com.itangcent.common.model.Request
import com.itangcent.common.utils.getPropertyValue
import io.swagger.v3.oas.models.media.Schema

class AnnoInfoAssemble {

    object SchemaAnnoAssemble {
        fun assembleInfo(schema: Schema<*>, request: Request, fieldName:String) {
            val body: LinkedHashMap<String, *>? = request.body?.let { it as? LinkedHashMap<String, *> }
            val schemaAnnoInfo = body?.get(Attrs.SCHEMA_ATTR)?.let { it as? LinkedHashMap<String, *> }
            schema.description = schemaAnnoInfo?.get(fieldName)?.getPropertyValue("description") as? String
        }
    }
}