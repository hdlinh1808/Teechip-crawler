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

    public CrawlerController(MainFrame frame) {
        this.frame = frame;
        LogPrinterManager.Instance.setMainFrameLog(frame.getLogPrinter());

        System.out.println(frame.getLogPrinter());
    }

    public void crawl(String url) {
        try {
            System.out.println(url);
            List<Item> items = CrawlerModel.Instance.crawlOneCategory(url);
            pushToServerV2(items);
        } catch (Exception ex) {
            Logger.getLogger(CrawlerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void pushToServer(Item item) throws JSONException, IOException {
        HttpPost httpPost = new HttpPost("http://localhost/duydl/index.php/wp-json/wc/v3/products");
        httpPost.addHeader("content-type", "application/json");

        String encoding = Base64.getEncoder()
                .encodeToString("ck_6c31e47de04da961391e80048077d86d9288c4b2:cs_c89f2085df2f1acbb3d87f7e8f9b7495791d3cbd".getBytes());
        httpPost.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
        httpPost.setEntity(new StringEntity(item.toJSON()));
        HttpResponse response = client.execute(httpPost);
        httpPost.completed();
        String result = HttpUtils.parseResultFromResponseHttpClient(response);
        System.out.println(result);
    }

    public void pushToServerV2(List<Item> items) throws JSONException {
        OAuthConfig config = new OAuthConfig("http://localhost/duydl/index.php",
                "ck_6c31e47de04da961391e80048077d86d9288c4b2",
                "cs_c89f2085df2f1acbb3d87f7e8f9b7495791d3cbd");
        WooCommerce wooCommerce = new WooCommerceAPI(config, ApiVersionType.V3);

        // Prepare object for request
        int count = 0;
        for (Item item : items) {
            System.out.println(++count);
            Map<String, Object> productInfo = new HashMap<>();
            productInfo.put("name", item.getProduct());
            productInfo.put("type", "simple");
            productInfo.put("regular_price", String.valueOf(item.getSizes().get(0).getPrice()));
            productInfo.put("description", "Testtest");
            JSONArray arr = new JSONArray();
            JSONObject json = new JSONObject();
            json.put("id", 6);
            json.put("position", 0);
            json.put("option", new String[]{"Black", "Green"});
            productInfo.put("attributes", arr.toString());

            // Make request and retrieve result
            Map product = wooCommerce.create(EndpointBaseType.PRODUCTS.getValue(), productInfo);
            break;
        }

    }
}
