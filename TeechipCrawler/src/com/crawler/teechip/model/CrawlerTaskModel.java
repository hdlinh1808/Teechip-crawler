/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crawler.teechip.model;

import com.crawler.teachip.common.JSoupUtils;
import com.crawler.teechip.entity.Item;
import static com.crawler.teechip.model.CrawlerModel.LIMIT;
import com.icoderman.woocommerce.oauth.OAuthConfig;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    
    private List<Item> getData(String domain, String path, String groupId) {
        String urlFormat = "%s/rest/retail-products/groups/%s%s?page=%d&limit=%d&recentViewAsShould=true";
        int count = 0;
        List<Item> items = new ArrayList<>();
        String[] part = path.split("/");
        String[] newPartPaths = new String[part.length - 2];
        for (int i = 2; i < part.length; i++) {
            newPartPaths[i - 2] = part[i];
        }

        String newPartPath = String.join(".", newPartPaths);
        String detailUrlFormat = JSoupUtils.MAIN_URL + "/campaigns/page/%d/shop/" + newPartPath + "/%s?retailProductCode=%s";
        while (true) {
            String url = String.format(urlFormat, JSoupUtils.MAIN_URL, groupId, path,
                    ++count, LIMIT);
//            LogPrinterManager.Instance.printInMainFrameLogArea(url);

            try {
                JSONObject rawData = CrawlerModel.Instance.getData(url);
                JSONArray retailProducts = rawData.getJSONArray("retailProducts");

                if (retailProducts.length() == 0) {
                    break;
                }

                JSONArray jPriceSize = new JSONArray();
                List<Item> partialItems = new ArrayList<>();
                for (int i = 0; i < retailProducts.length(); i++) {
                    JSONObject product = retailProducts.getJSONObject(i);
                    Item item = CrawlerModel.Instance.getItemData(product);
                    jPriceSize.put(item.getProductId());
                    partialItems.add(item);
//                    String itemUrl = String.format(detailUrlFormat, count, item.getCampaignUrl(), item.getCode());
//                    System.out.println(itemUrl);
//                    getDescription(itemUrl);

                }

                jPriceSize = CrawlerModel.Instance.getDataPrice(jPriceSize);
                for (int i = 0; i < partialItems.size(); i++) {
                    JSONObject jData = jPriceSize.getJSONObject(i).getJSONObject("data");
                    Iterator<String> keys = jData.keys();
                    List<Item.Size> sizes = new ArrayList<>();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        JSONObject jSize = jData.getJSONObject(key);
                        int price = jSize.getInt("fees") + jSize.getInt("base");
                        Item.Size size = new Item.Size(key, price);
                        sizes.add(size);
                    }
                    partialItems.get(i).setSizes(sizes);
                }

                items.addAll(partialItems);

            } catch (Exception ex) {
                Logger.getLogger(CrawlerModel.class.getName()).log(Level.SEVERE, null, ex);
            }
            break;
        }
        return items;
    }
}
