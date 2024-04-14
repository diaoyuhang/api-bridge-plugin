package com.itangcent.idea.plugin.dialog;

import javax.swing.*;

class RemoteServerConfigGUI {
    private lateinit var rootJPanel: JPanel
    private lateinit var token: JTextField
    private lateinit var serverId: JTextField
    private lateinit var domain:JTextField

    fun getRootPanel(): JComponent {
        return rootJPanel
    }

    fun getToken(): JTextField {
        return token
    }

    fun getServerId(): JTextField {
        return serverId
    }

    fun getDomain():JTextField{
        return domain
    }
}
