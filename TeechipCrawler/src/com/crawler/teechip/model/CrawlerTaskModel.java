/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crawler.teechip.model;

import com.crawler.teachip.common.JSoupUtils;
import com.crawler.teechip.entity.Item;
import com.icoderman.woocommerce.oauth.OAuthConfig;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.http.client.HttpClient;
import org.json.JSONException;

/**
 *
 * @author linhhd
 */
public class CrawlerTaskModel {
    private HttpClient client;
    private int limit = 50;
    private String crawlUrl;
    
    private OAuthConfig config;
    
    
    public CrawlerTaskModel(String myUrl, String consumerKey, String consumerSecret, String crawlUrl){
        config = new OAuthConfig(myUrl, consumerKey, consumerSecret);
        this.crawlUrl = crawlUrl;
    }
    
    public List<Item> crawlOneCategory(String url) throws URISyntaxException, IOException, JSONException {
        URI uri = new URI(url);
        String path = uri.getPath();
        CrawlerModel.Instance.genCategory(path);
        String groupId = JSoupUtils.getGroupCode();
        return getData(JSoupUtils.MAIN_URL, path, groupId);
    }
    
}
