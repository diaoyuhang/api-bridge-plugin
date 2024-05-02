package com.itangcent.utils

import kotlin.reflect.KClass

interface JsonSupport {

    fun toJson(obj: Any?): String

    fun <T : Any> fromJson(json:String, cls: KClass<T>):T
}