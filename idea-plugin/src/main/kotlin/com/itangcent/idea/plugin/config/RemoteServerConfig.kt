package com.itangcent.idea.plugin.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "RemoteServerConfig",storages = [Storage("plugin.xml")])
class RemoteServerConfig : PersistentStateComponent<MutableMap<String,String>> {
    var configMap: MutableMap<String, String> = mutableMapOf()

    override fun getState(): MutableMap<String, String> {
        return configMap
    }

    override fun loadState(p0: MutableMap<String,String>) {
        configMap=p0
    }
}