package com.itangcent.idea.plugin.dialog;

import javax.swing.*;

public class RemoteServerConfigGUI {
    private JPanel rootJPanel;
    private JTextField token;
    private JTextField serverId;

    public RemoteServerConfigGUI() {
    }

    public JPanel getRootJPanel() {
        return rootJPanel;
    }

    public void setRootJPanel(JPanel rootJPanel) {
        this.rootJPanel = rootJPanel;
    }

    public JTextField getToken() {
        return token;
    }

    public void setToken(JTextField token) {
        this.token = token;
    }

    public JTextField getServerId() {
        return serverId;
    }

    public void setServerId(JTextField serverId) {
        this.serverId = serverId;
    }
}
