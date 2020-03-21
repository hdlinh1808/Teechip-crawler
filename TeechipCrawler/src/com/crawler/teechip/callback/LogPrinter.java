/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crawler.teechip.callback;

import javax.swing.JTextArea;

/**
 *
 * @author doleduy
 */
public class LogPrinter implements Callback{
    
    JTextArea txtLog;

    public LogPrinter(JTextArea txtLog) {
        this.txtLog = txtLog;
    }

    @Override
    public void doTask() {
        
    }
    
    public void printLog(String message){
        txtLog.append(message);
        txtLog.append("\n");
    }
}
