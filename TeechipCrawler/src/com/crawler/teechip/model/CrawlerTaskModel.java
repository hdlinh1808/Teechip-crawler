/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crawler.teechip.model;

import com.crawler.teachip.common.JSoupUtils;
import com.crawler.teechip.callback.LogPrinter;
import com.crawler.teechip.common.LogPrinterManager;
import com.crawler.teechip.entity.AttributeOption;
import com.crawler.teechip.entity.CategoryData;
import com.crawler.teechip.entity.Image;
import com.crawler.teechip.entity.Item;
import com.crawler.teechip.entity.LocalProductAttribute;
import com.crawler.teechip.executortask.CrawlTask;
import com.crawler.teechip.executortask.PushTask;
import com.crawler.teechip.main.MainFrame;
import static com.crawler.teechip.model.CrawlerModel.COLOR_ID;
import static com.crawler.teechip.model.CrawlerModel.LIMIT;
import static com.crawler.teechip.model.CrawlerModel.SIZE_ID;
import static com.crawler.teechip.model.CrawlerModel.config;
import static com.crawler.teechip.model.CrawlerModel.mapCategoryName;
import com.icoderman.woocommerce.ApiVersionType;
import com.icoderman.woocommerce.WooCommerce;
import com.icoderman.woocommerce.WooCommerceAPI;
import com.icoderman.woocommerce.oauth.OAuthConfig;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
    private String category;
    private String newPartPath;

    LogPrinter logPrinter;
    List<ExecutorService> executorServices = new ArrayList();

    public CrawlerTaskModel(String myUrl, String consumerKey, String consumerSecret, String crawlUrl) {
        config = new OAuthConfig(myUrl, consumerKey, consumerSecret);
        this.crawlUrl = crawlUrl;
    }

    public List<Item> crawlOneCategory(String url) throws URISyntaxException, IOException, JSONException {
        URI uri = new URI(url);
        String path = uri.getPath();
        CrawlerModel.Instance.genCategory(path);
        String groupId = JSoupUtils.getGroupCode();

        String[] part = path.split("/");
        String[] newPartPaths = new String[part.length - 2];
        for (int i = 2; i < part.length; i++) {
            newPartPaths[i - 2] = part[i];
        }
        newPartPath = String.join(".", newPartPaths);
        newPartPath = String.join(".", newPartPaths);
        category = newPartPaths[newPartPaths.length - 1];

        return getData(JSoupUtils.MAIN_URL, path, groupId);
    }

    private List<Item> getData(String domain, String path, String groupId) {
        String urlFormat = "%s/rest/retail-products/groups/%s%s?page=%d&limit=%d&recentViewAsShould=true";
        int count = 0;
        List<Item> items = new ArrayList<>();

        String detailUrlFormat = JSoupUtils.MAIN_URL + "/campaigns/page/%d/shop/" + newPartPath + "/%s?retailProductCode=%s";
        List<Thread> tasks = new ArrayList<>();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);
        executorServices.add(executor);
        while (true) {
            String url = String.format(urlFormat, JSoupUtils.MAIN_URL, groupId, path,
                    ++count, LIMIT);

            if (count > 25) {
                break;
            }

            Thread thread = new Thread(new CrawlTask(url, items, count, LIMIT));
            tasks.add(thread);
            executor.execute(thread);

        }

        awaitTerminationAfterShutdown(executor);
        return items;
    }

    public void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(3600, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void pushProductToServer(List<Item> items) {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);
        executorServices.add(executor);
        for (Item item : items) {
            PushTask task = new PushTask(item, category, this);
            executor.execute(task);
        }
        executor.shutdown();
    }

    public void pushProductToServer(Item item, String category) {
        try {
            Map<String, Object> productInfo = new HashMap<>();
            productInfo.put("name", item.getDesign() + " " + item.getProduct());
            productInfo.put("type", "variable");
            productInfo.put("regular_price", item.getPrice() + "");
            productInfo.put("description", "desciption");
            productInfo.put("short_description", "short_desciption");
            List<CategoryData> categories = new ArrayList();
            categories.add(new CategoryData(mapCategoryName.get(category)));
            productInfo.put("categories", categories);
            List<Image> images = new ArrayList<>();
            for (String imageUrl : item.getImageUrl()) {
                images.add(new Image(imageUrl));
            }

            List<LocalProductAttribute> attributes = new ArrayList<>();
            attributes.add(new LocalProductAttribute(COLOR_ID, item.getAllColor()));
            attributes.add(new LocalProductAttribute(SIZE_ID, item.getAllSize()));
            productInfo.put("attributes", attributes);
            productInfo.put("images", images);
            WooCommerce wooCommerce = new WooCommerceAPI(config, ApiVersionType.V3);
            Map<String, Object> response = wooCommerce.create("products", productInfo);
            Integer id = (Integer) response.get("id");
            cloneVariations(item, id);
            LogPrinterManager.Instance.printInMainFrameLogArea("Pushing " + item.getDesign() + " " + item.getProduct() + ": done.\n");
        } catch (Exception ex) {

        }

    }

    private void cloneVariations(Item item, int productId) {
        Map<String, Object> variationInfo;
        List<AttributeOption> attributeOptions;
        AttributeOption colorOption;
        AttributeOption sizeOption;
        for (Item.Size size : item.getSizes()) {
            attributeOptions = new ArrayList();
            int price = size.getPrice();
            sizeOption = new AttributeOption(SIZE_ID, size.getSize());
            attributeOptions.add(sizeOption);
            variationInfo = new HashMap<>();
            variationInfo.put("regular_price", String.valueOf(price));
            variationInfo.put("attributes", attributeOptions);
            WooCommerce wooCommerce = new WooCommerceAPI(config, ApiVersionType.V3);
            Map<String, Object> resp = wooCommerce.create("products/" + productId + "/variations", variationInfo);
        }
    }
    
    
    
    public void shutdownNow(){
        for(ExecutorService executor: executorServices){
            try {
                MainFrame.tapCancel = true;
                executor.awaitTermination(0, TimeUnit.MILLISECONDS);
                System.out.println("shut downdone");
            } catch (Exception ex) {
                Logger.getLogger(CrawlerTaskModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public boolean isTerminate(){
        return executorServices.get(0).isTerminated();
    }
}
