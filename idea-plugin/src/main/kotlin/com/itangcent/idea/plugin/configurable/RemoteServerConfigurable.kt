package com.itangcent.idea.plugin.configurable

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.itangcent.idea.plugin.config.RemoteServerConfig
import com.itangcent.idea.plugin.dialog.RemoteServerConfigGUI
import javax.swing.JComponent

class RemoteServerConfigurable : SearchableConfigurable  {
    private val remoteServerConfigGUI = RemoteServerConfigGUI()


    override fun createComponent(): JComponent? {
        return remoteServerConfigGUI.getRootPanel()
    }


    override fun isModified(): Boolean {
        return true;
    }

    override fun apply() {
        RemoteServerConfig.configMap["项目.id"] = remoteServerConfigGUI.getServerId().getText()
        RemoteServerConfig.configMap["token"] = remoteServerConfigGUI.getToken().getText()
    }

    override fun getDisplayName(): String {
        return "ServerConfig"
    }

    override fun getId(): String {
        return "ServerConfig"
    }

}