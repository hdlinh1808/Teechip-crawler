/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crawler.teechip.controller;

import com.crawler.teachip.common.HttpUtils;
import com.crawler.teechip.common.LogPrinterManager;
import com.crawler.teechip.entity.Item;
import com.crawler.teechip.main.MainFrame;
import com.crawler.teechip.model.CrawlerModel;
import static com.crawler.teechip.model.CrawlerModel.client;
import com.crawler.teechip.model.CrawlerTaskModel;
import com.icoderman.woocommerce.ApiVersionType;
import com.icoderman.woocommerce.EndpointBaseType;
import com.icoderman.woocommerce.WooCommerce;
import com.icoderman.woocommerce.WooCommerceAPI;
import com.icoderman.woocommerce.oauth.OAuthConfig;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author linhhd
 */
public class CrawlerController {

    private MainFrame frame;
    CrawlerTaskModel task;
    Thread thread;

    public CrawlerController(MainFrame frame, String crawlUrl, String consumerKey, String consumerSecret, String myUrl) {
        this.frame = frame;
        LogPrinterManager.Instance.setMainFrameLog(frame.getLogPrinter());
        task = new CrawlerTaskModel(myUrl, consumerKey, consumerSecret, crawlUrl);
    }

    public void crawl(String crawlUrl, String consumerKey, String consumerSecret, String myUrl) {
        try {
            thread = new Thread() {
                @Override
                public void run() {
                    try {
                        task = new CrawlerTaskModel(myUrl, consumerKey, consumerSecret, crawlUrl);
                        LogPrinterManager.Instance.printInMainFrameLogArea("Start crawling...\n");
                        List<Item> items = task.crawlOneCategory(crawlUrl);
                        LogPrinterManager.Instance.printInMainFrameLogArea("Crawling done!\n");
                        LogPrinterManager.Instance.printInMainFrameLogArea("Start pushing to server..\n");
                        System.out.println("itemsize: " + items.size());
                        task.pushProductToServer(items);
                        frame.enableCrawlButton();
                    } catch (Exception ex) {
                        
                    }
                }
            };
            thread.start();
            
        } catch (Exception ex) {
            Logger.getLogger(CrawlerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void shutDown() {
        if(thread != null){
            thread.interrupt();
        }
        
        task.shutdownNow();
        frame.cancelDone();
    }
    

}
