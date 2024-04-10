package com.itangcent.idea.plugin.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "RemoteServerConfig", storages = [Storage("RemoteServerConfig.xml")])
class RemoteServerConfig : PersistentStateComponent<ServerConfigDataState> {
    private var serverConfigDataState = ServerConfigDataState()

    companion object{
        fun getInstance():RemoteServerConfig{
            return ServiceManager.getService(RemoteServerConfig::class.java)
        }
    }
    override fun getState(): ServerConfigDataState {
        return serverConfigDataState
    }

    override fun loadState(p0: ServerConfigDataState) {
        serverConfigDataState=p0
    }
}