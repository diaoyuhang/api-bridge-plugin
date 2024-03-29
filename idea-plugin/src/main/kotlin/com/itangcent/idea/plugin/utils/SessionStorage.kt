package com.itangcent.idea.plugin.utils

import com.google.inject.Singleton
import com.itangcent.annotation.script.ScriptTypeName
import com.itangcent.common.utils.sub
import com.itangcent.idea.plugin.utils.Storage.Companion.DEFAULT_GROUP

/**
 * Implementation of [Storage] based on memory
 * The [SessionStorage] can only be accessed in the current action.
 */
@Singleton
@ScriptTypeName("session")
class SessionStorage : AbstractStorage() {

    private val data: MutableMap<String, Any?> by lazy { linkedMapOf() }

    override fun getCache(group: String): MutableMap<String, Any?> {
        return data.sub(group)
    }

    override fun onUpdate(group: String?, cache: MutableMap<String, Any?>) {
        if (cache.isEmpty()) {
            data.remove(group ?: DEFAULT_GROUP)
        } else {
            data[group ?: DEFAULT_GROUP] = cache
        }
    }
}
