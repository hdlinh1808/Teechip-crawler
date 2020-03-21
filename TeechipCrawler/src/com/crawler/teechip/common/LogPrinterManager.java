/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crawler.teechip.common;

import com.crawler.teechip.callback.LogPrinter;

/**
 *
 * @author doleduy
 */
public class LogPrinterManager {
    public static LogPrinterManager Instance = new LogPrinterManager();
    public LogPrinter mainFrameLog;
    
    public void setMainFrameLog(LogPrinter printer){
        mainFrameLog = printer;
    }
    
    private LogPrinterManager(){
        
    }
    
    public void printInMainFrameLogArea(String message){
        mainFrameLog.printLog(message);
    }
}
