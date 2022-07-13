package com.hexagram2021.autoupdate;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.instrument.Instrumentation;

public class AutoUpdate {
    private static final DefaultTableModel Panel = new DefaultTableModel();
    private static final JTable table = new JTable(Panel);

    public static void premain(String agentArg, Instrumentation inst) {
        JFrame frame = new JFrame();

        JScrollPane pane = new JScrollPane(table);
        Panel.addColumn("<html><body><p align=\"center\">正在更新客户端</p></body></html>");
        frame.add(pane);

        frame.setTitle("LSC 自动升级程序");
        frame.setSize(800, 450);
        frame.setVisible(true);

        if(agentArg == null || agentArg.equals("") || agentArg.equals("help")) {
            AutoUpdate.log("Usage: -javaagent:AutoUpdate=<url>");
        } else if (agentArg.equals("$genConfig")) {
            genConfig();
        } else {
            ConfigHelper.URL = agentArg;
            update();
        }
    }

    private static void update() {
        ConfigHelper config = new ConfigHelper(true);
        if(config.getStatus()) {
            HttpHelper http = new HttpHelper(config);
            if(http.getStatus()) {
                AutoUpdate.log("Successfully updated");
            }
        }
    }

    private static void genConfig() {
        ConfigHelper config = new ConfigHelper(false);
        if(config.getStatus()) {
            AutoUpdate.log("Successfully generated config.js");
        }
    }

    public static void log(String msg) {
        Panel.insertRow(0, new String[]{"<html><body><p>" + msg + "</p></body></html>"});
        table.updateUI();
    }

    public static void err(String msg) {
        Panel.insertRow(0, new String[]{"<html><body><font color=#cf0f0f>" + msg + "</font></body></html>"});
        table.updateUI();
    }
}
