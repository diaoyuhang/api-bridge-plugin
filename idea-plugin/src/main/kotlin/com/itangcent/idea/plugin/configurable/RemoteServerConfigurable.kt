package com.itangcent.idea.plugin.configurable

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.itangcent.idea.plugin.config.RemoteServerConfig
import com.itangcent.idea.plugin.dialog.RemoteServerConfigGUI
import javax.swing.JComponent

class RemoteServerConfigurable(myProject: Project?) : SearchableConfigurable  {
    private val remoteServerConfigGUI = RemoteServerConfigGUI()


    override fun createComponent(): JComponent? {
        return remoteServerConfigGUI.rootJPanel
    }


    override fun isModified(): Boolean {
        return true;
    }

    override fun apply() {
        RemoteServerConfig.configMap["项目.id"] = remoteServerConfigGUI.serverId.getText()
        RemoteServerConfig.configMap["token"] = remoteServerConfigGUI.token.getText()
    }

    override fun getDisplayName(): String {
        return "ServerConfig"
    }

    override fun getId(): String {
        return "ServerConfig"
    }

}