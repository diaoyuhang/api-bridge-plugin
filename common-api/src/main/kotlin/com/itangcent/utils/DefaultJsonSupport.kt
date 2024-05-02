package com.itangcent.utils

import com.itangcent.common.utils.GsonUtils
import kotlin.reflect.KClass

object DefaultJsonSupport : JsonSupport {
    override fun toJson(obj: Any?): String {
        return GsonUtils.toJson(obj)
    }

    override fun <T : Any> fromJson(json: String, cls: KClass<T>): T {
        return GsonUtils.fromJson(json,cls)
    }
}