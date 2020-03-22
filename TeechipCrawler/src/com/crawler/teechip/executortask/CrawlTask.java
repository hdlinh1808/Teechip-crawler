/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crawler.teechip.executortask;

import com.crawler.teechip.entity.Item;
import com.crawler.teechip.model.CrawlerModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author linhhd
 */
public class CrawlTask implements Runnable{
        
        String url;
        List<Item> listAllItem;

        public CrawlTask(String url, List<Item> listAllItem) {
            this.url = url;
            this.listAllItem = listAllItem;
        }

        @Override
        public void run() {
            try {
                JSONObject rawData = CrawlerModel.Instance.getData(url);
                JSONArray retailProducts = rawData.getJSONArray("retailProducts");

                if (retailProducts.length() == 0) {
                    return;
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
                
                synchronized(listAllItem){
                    listAllItem.addAll(partialItems);
                }
                System.out.println(url + ": done.");

            } catch (Exception ex) {
                Logger.getLogger(CrawlerModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
