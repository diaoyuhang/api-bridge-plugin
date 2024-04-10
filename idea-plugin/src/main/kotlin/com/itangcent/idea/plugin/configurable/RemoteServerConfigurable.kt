package com.itangcent.idea.plugin.configurable

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.itangcent.idea.plugin.config.RemoteServerConfig
import com.itangcent.idea.plugin.dialog.RemoteServerConfigGUI
import javax.swing.JComponent

class RemoteServerConfigurable(myProject: Project) : SearchableConfigurable  {
    private val remoteServerConfigGUI = RemoteServerConfigGUI()
    val project=myProject

//    private val remoteServerConfig: RemoteServerConfig = project.getService(RemoteServerConfig::class.java)
    override fun createComponent(): JComponent {
        val remoteServerConfig = RemoteServerConfig.getInstance()
        if (remoteServerConfig.state.configMap["${project.name}.id"] != null) {
            remoteServerConfigGUI.getServerId().text = remoteServerConfig.state.configMap["${project.name}.id"]
        }

        if (remoteServerConfig.state.configMap["token"] != null) {
            remoteServerConfigGUI.getToken().text = remoteServerConfig.state.configMap["token"]
        }
        return remoteServerConfigGUI.getRootPanel()
    }


    override fun isModified(): Boolean {
        return true;
    }

    override fun apply() {
        val remoteServerConfig = RemoteServerConfig.getInstance()
        remoteServerConfig.state.configMap["${project.name}.id"] = remoteServerConfigGUI.getServerId().getText()
        remoteServerConfig.state.configMap["token"] = remoteServerConfigGUI.getToken().getText()
    }

    override fun getDisplayName(): String {
        return "ServerConfig"
    }

    override fun getId(): String {
        return "ServerConfig"
    }

}