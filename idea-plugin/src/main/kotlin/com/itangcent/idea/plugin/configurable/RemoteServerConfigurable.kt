package com.itangcent.idea.plugin.configurable

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.itangcent.idea.plugin.config.RemoteServerConfig
import com.itangcent.idea.plugin.dialog.RemoteServerConfigGUI
import javax.swing.JComponent

class RemoteServerConfigurable(myProject: Project) : SearchableConfigurable  {
    private val remoteServerConfigGUI = RemoteServerConfigGUI()
    val project=myProject

    private val remoteServerConfig: RemoteServerConfig = project.getService(RemoteServerConfig::class.java)
    override fun createComponent(): JComponent {
        if (remoteServerConfig!!.configMap["${project.name}.id"] != null) {
            remoteServerConfigGUI.getServerId().text = remoteServerConfig!!.configMap["${project.name}.id"]
        }

        if (remoteServerConfig.configMap["token"] != null) {
            remoteServerConfigGUI.getToken().text = remoteServerConfig!!.configMap["token"]
        }
        return remoteServerConfigGUI.getRootPanel()
    }


    override fun isModified(): Boolean {
        return true;
    }

    override fun apply() {
        remoteServerConfig!!.configMap["${project.name}.id"] = remoteServerConfigGUI.getServerId().getText()
        remoteServerConfig!!.configMap["token"] = remoteServerConfigGUI.getToken().getText()
    }

    override fun getDisplayName(): String {
        return "ServerConfig"
    }

    override fun getId(): String {
        return "ServerConfig"
    }

}