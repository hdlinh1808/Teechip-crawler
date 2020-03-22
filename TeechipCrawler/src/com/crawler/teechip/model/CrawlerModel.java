/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crawler.teechip.model;

import com.crawler.teachip.common.HttpUtils;
import com.crawler.teachip.common.JSoupUtils;
import com.crawler.teechip.entity.Category;
import com.crawler.teechip.entity.Item;
import com.icoderman.woocommerce.ApiVersionType;
import com.icoderman.woocommerce.EndpointBaseType;
import com.icoderman.woocommerce.WooCommerce;
import com.icoderman.woocommerce.WooCommerceAPI;
import com.icoderman.woocommerce.oauth.OAuthConfig;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author linhhd
 */
public class CrawlerModel {

    public static final CrawlerModel Instance = new CrawlerModel();
    public static HttpClient client;
    public static int LIMIT = 50;
    public static String BASE_IMAGEURL = "https://cdn.32pt.com/";
    public static String COLOR_STRING = "Color";
    public static String SIZE_STRING = "Size";
    public static int COLOR_ID = -1;
    public static int SIZE_ID = -1;
    public static Map<String, Integer> mapCategoryName = new HashMap<>();

    public static OAuthConfig config = new OAuthConfig("http://localhost/duydl/index.php",
            "ck_6c31e47de04da961391e80048077d86d9288c4b2",
            "cs_c89f2085df2f1acbb3d87f7e8f9b7495791d3cbd");

    private CrawlerModel() {
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(15 * 1000).build();//time out 10s
        client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();

    }

    public void crawl() {
        try {
            Map<String, JSONArray> result = new HashMap();
            List<Category> categories = JSoupUtils.getAllCategory();
            String groupId = JSoupUtils.getGroupCode();
            for (Category category : categories) {
                for (Category subCategory : category.getSubCategories()) {
                    List<Category> subsubCategories = subCategory.getSubCategories();
                    if (subsubCategories == null) {
                        continue;
                    }

                    for (Category subsubCategory : subsubCategories) {
                        System.out.println(subsubCategory);
                        getData(subsubCategory, groupId);
                        break;
                    }
                    break;
                }
                break;
            }
        } catch (IOException ex) {
            Logger.getLogger(CrawlerModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CrawlerModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<Item> crawlOneCategory(String url) throws URISyntaxException, IOException, JSONException {

        URI uri = new URI(url);
        String path = uri.getPath();
        genCategory(path);
        String groupId = JSoupUtils.getGroupCode();
        return getData(JSoupUtils.MAIN_URL, path, groupId);
    }

    public void genCategory(String path) {
        String[] part = path.split("/");
        WooCommerce wooCommerce = new WooCommerceAPI(config, ApiVersionType.V3);
        List categories = wooCommerce.getAll("products/categories");
        String[] newPartPaths = new String[part.length - 2];

        Map<String, Object> categoryInfo = new HashMap<>();
        for (int i = 2; i < part.length; i++) {
            String name = part[i];

            if (mapCategoryName.containsKey(name)) {
                continue;
            }
            categoryInfo.put("name", genCategoryName(name));
            if (i > 2) {
                categoryInfo.put("parent", mapCategoryName.get(part[i - 1]));
            }
            categoryInfo.put("name", genCategoryName(name));

            Map<String, Object> newCategory = wooCommerce.create("products/categories/", categoryInfo);
            Integer newId = (Integer) newCategory.get("id");
            mapCategoryName.put(name, newId);
        }
    }

    private String genCategoryName(String rawName) {
        return rawName;
    }

    public void init() {
        initAllAttribute();
        initAllCategories();
    }

    public void initAllCategories() {
        WooCommerce wooCommerce = new WooCommerceAPI(config, ApiVersionType.V3);
        List categories = wooCommerce.getAll("products/categories");
        Map<String, Object> result;
        for (Object o : categories) {
            result = (Map<String, Object>) o;
            mapCategoryName.put((String) result.get("name"), (Integer) result.get("id"));
        }
    }

    private void initAllAttribute() {
        WooCommerce wooCommerce = new WooCommerceAPI(config, ApiVersionType.V3);
        List attrs = wooCommerce.getAll("products/attributes");
        for (Object attr : attrs) {
            Map map = (Map) attr;
            if (map.get("name").equals(COLOR_STRING)) {

                System.out.println("contain color");
                COLOR_ID = (int) map.get("id");
            }

            if (map.get("name").equals(SIZE_STRING)) {
                System.out.println("contain size");
                SIZE_ID = (int) map.get("id");
            }
        }

        if (COLOR_ID == -1) {
            String[] colors = new String[]{"Navy", "Red", "Royal", "Sports Grey"};
            COLOR_ID = initAttribute(COLOR_STRING, colors);
        }

        if (SIZE_ID == -1) {
//            String[] sizes = new String[]{"S", "M", "L", "XL", "2XL", "3XL", "4XL", "5XL", "6XL"};
            String[] sizes = new String[]{"lrg", "med", "sml", "xlg", "xxl", "3XL", "4XL", "5XL", "6XL"};
            SIZE_ID = initAttribute(SIZE_STRING, sizes);
        }

    }

    private int initAttribute(String name, String[] values) {

        //init Color
        Map<String, Object> attribute = new HashMap<>();
        attribute.put("name", name);
        attribute.put("slug", "pa_" + name.toLowerCase());
        attribute.put("type", "select");
        attribute.put("order_by", "menu_order");
        attribute.put("has_archives", true);
        WooCommerce wooCommerce = new WooCommerceAPI(config, ApiVersionType.V3);
        Map attributeResult = wooCommerce.create("products/attributes", attribute);
        Integer attrId = (Integer) attributeResult.get("id");
//        System.out.println(attributeResult);
        Map<String, Object> mapValue;
        for (String value : values) {
            mapValue = new HashMap<>();
            mapValue.put("name", value);
            wooCommerce.create("products/attributes/" + attrId + "/terms", mapValue);

        }
        return attrId;
    }

    public void crawlOneItem(String url) {
        JSONObject json = null;
        try {
            json = JSoupUtils.getScriptData(url);
            JSONObject vias = json.getJSONObject("vias");
            JSONObject Campaign = vias.getJSONObject("Campaign");
            JSONObject docs = Campaign.getJSONObject("docs");
            JSONObject id = docs.getJSONObject("id");
            String key = (String) id.keys().next();
            JSONArray related = id.getJSONObject(key).getJSONObject("doc").getJSONArray("related");
            System.out.println(related);

        } catch (Exception ex) {
            ex.printStackTrace();
//            System.out.println(json.toString());
//            System.out.println("err");
        }
    }

    private JSONArray getData(Category category, String groupId) {
        String urlFormat = "%s/rest/retail-products/groups/%s%s?page=%d&limit=%d&recentViewAsShould=true";
        int count = 0;

        while (true) {
            if (count > 5) {
                break;
            }
            String url = String.format(urlFormat, JSoupUtils.MAIN_URL, groupId, category.getRelativeLink(),
                    ++count, LIMIT);

            try {
                JSONObject rawData = getData(url);
                System.out.println(rawData.toString());
            } catch (Exception ex) {
                Logger.getLogger(CrawlerModel.class.getName()).log(Level.SEVERE, null, ex);
            }

            break;
        }

        return null;
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
                JSONObject rawData = getData(url);
                JSONArray retailProducts = rawData.getJSONArray("retailProducts");

                if (retailProducts.length() == 0) {
                    break;
                }

                JSONArray jPriceSize = new JSONArray();
                List<Item> partialItems = new ArrayList<>();
                for (int i = 0; i < retailProducts.length(); i++) {
                    JSONObject product = retailProducts.getJSONObject(i);
                    Item item = getItemData(product);
                    jPriceSize.put(item.getProductId());
                    partialItems.add(item);
//                    String itemUrl = String.format(detailUrlFormat, count, item.getCampaignUrl(), item.getCode());
//                    System.out.println(itemUrl);
//                    getDescription(itemUrl);

                }

                jPriceSize = getDataPrice(jPriceSize);
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

    private Item getItemData(JSONObject rawData) throws JSONException, URISyntaxException {
        int price = rawData.getInt("price");
        JSONObject jNames = rawData.getJSONObject("names");
        String product = jNames.getString("product");
        String design = jNames.getString("design");

        JSONArray jImages = rawData.getJSONArray("images");
        String code = rawData.getString("code");
        String[] images = new String[jImages.length()];
        String campaignUrl = rawData.getString("campaignUrl");
        String productId = rawData.getString("_id");
        String color = rawData.getString("color");

        for (int i = 0; i < jImages.length(); i++) {
            JSONObject jImage = jImages.getJSONObject(i);
            String prefix = jImage.getString("prefix");
            URI uri = new URI(prefix);
            String path = uri.getPath();
            String newUrl = BASE_IMAGEURL + path + "/regular.jpg";
            images[i] = newUrl;
        }

        JSONArray jRelateds = rawData.getJSONArray("related");
        List<Item.Relation> relations = new ArrayList<>();
        Item.Relation relation;
        String reColor;
        String reCode;
        for (int i = 0; i < jRelateds.length(); i++) {
            JSONObject jRelated = jRelateds.getJSONObject(i);
            reColor = jRelated.getJSONObject("detail").getString("color");
            reCode = jRelated.getString("code");
            relation = new Item.Relation(reColor, reCode);
            relations.add(relation);
        }

        Item item = new Item(price, product, design);
        item.setImageUrl(images);
        item.setCode(code);
        item.setCampaignUrl(campaignUrl);
        item.setProductId(productId);
        item.setColor(color);
        item.setRelations(relations);
        item.setPrice(price);
        return item;
        //get relate
    }

    private JSONObject getData(String url) throws IOException, JSONException {
        HttpGet httpGet = new HttpGet(url);
        HttpResponse httpResponse = client.execute(httpGet);
        String result = HttpUtils.parseResultFromResponseHttpClient(httpResponse);
        httpGet.completed();
        httpGet.releaseConnection();
        httpResponse.getEntity().getContent().close();
        return new JSONObject(result);
    }

    private static String getDescription(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements wrapper = doc.select(".p-0.custom-bullets");
        Elements eles = wrapper.select("li");
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Element ele : eles) {
            count++;
            sb.append(ele.text()).append("\n");
        }
        System.out.println(count);
        return "";
    }

    private static JSONArray getDataPrice(JSONArray params) throws UnsupportedEncodingException, IOException, JSONException {
        HttpPost httpPost = new HttpPost("https://teechip.com/rest/retail-products/pricing");
        httpPost.addHeader("content-type", "application/json");
        JSONArray arr = new JSONArray();
//        arr.put("5b20ae4ce921b2402f57ec25");
        httpPost.setEntity(new StringEntity(params.toString()));
        HttpResponse response = client.execute(httpPost);
        httpPost.completed();
        String result = HttpUtils.parseResultFromResponseHttpClient(response);
        return new JSONArray(result);
    }

    public void pushProductToServer(List<Item> items, String category) {
        for (Item item : items) {
            pushProductToServer(item, category);
            break;
        }
    }

    public void pushProductToServer(Item item, String category) {
        Map<String, Object> productInfo = new HashMap<>();
        productInfo.put("name", item.getDesign() + " " + item.getProduct());
        productInfo.put("type", "variable");
        productInfo.put("regular_price", item.getPrice() + "");
        productInfo.put("description", "desciption");
        productInfo.put("short_description", "short_desciption");
//        productInfo.put("sku", item.getProductId());
//        productInfo.put("id", item.getProductId());
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
        System.out.println(id);
        cloneVariations(item, id);
    }

    public void cloneVariations(Item item, int productId) {
        Map<String, Object> variationInfo;
        List<AttributeOption> attributeOptions;
//        WooCommerce wooCommerce = new WooCommerceAPI(config, ApiVersionType.V3);
        AttributeOption colorOption;
        AttributeOption sizeOption;
        for (Item.Size size : item.getSizes()) {
            attributeOptions = new ArrayList();
            int price = size.getPrice();
            sizeOption = new AttributeOption(SIZE_ID, size.getSize());

//            for (Item.Relation relation : item.getRelations()) {
//                variationInfo = new HashMap<>();
//                colorOption = new AttributeOption(COLOR_ID, relation.getColor());
//                variationInfo.put("regular_price", String.valueOf(price));
//                variationInfo.put("image", new Image("https://images-eu.ssl-images-amazon.com/images/I/31oIZDvTgFL._SY300_QL70_ML2_.jpg"));
//                attributeOptions = new ArrayList<>();
//                attributeOptions.add(sizeOption);
//                attributeOptions.add(colorOption);
//                variationInfo.put("attributes", attributeOptions);
//                Map<String, Object> resp = wooCommerce.create("products/" + productId + "/variations", variationInfo);
////                System.out.println(resp);
//            }
            attributeOptions.add(sizeOption);
            variationInfo = new HashMap<>();
            variationInfo.put("regular_price", String.valueOf(price));
            variationInfo.put("attributes", attributeOptions);
            WooCommerce wooCommerce = new WooCommerceAPI(config, ApiVersionType.V2);
            Map<String, Object> resp = wooCommerce.create("products/" + productId + "/variations", variationInfo);
        }

//        wooCommerce.update(SIZE_STRING, LIMIT, variationInfo)
    }

    public static void pushToServer(Item item) throws JSONException, IOException {
        HttpPost httpPost = new HttpPost("http://localhost/duydl/index.php/wp-json/wc/v3/products");
        httpPost.addHeader("content-type", "application/json");

//        client.
        httpPost.setEntity(new StringEntity(item.toJSON()));
        HttpResponse response = client.execute(httpPost);
        httpPost.completed();
        String result = HttpUtils.parseResultFromResponseHttpClient(response);
        System.out.println(result);
    }

    public static void pushToServerV2(Item item) throws JSONException {
        OAuthConfig config = new OAuthConfig("http://localhost/duydl/index.php",
                "ck_6c31e47de04da961391e80048077d86d9288c4b2",
                "cs_c89f2085df2f1acbb3d87f7e8f9b7495791d3cbd");
        WooCommerce wooCommerce = new WooCommerceAPI(config, ApiVersionType.V3);

        // Prepare object for request
        Map<String, Object> productInfo = new HashMap<>();
//        productInfo.put("name", "Color1");
//        productInfo.put("slug", "pa_color");
//        productInfo.put("type", "select");
//        productInfo.put("order_by", "menu_order");
//        productInfo.put("has_achieves", true);

        productInfo.put("regular_price", "9.00");
        Image image = new Image("https://images-eu.ssl-images-amazon.com/images/I/31oIZDvTgFL._SY300_QL70_ML2_.jpg");

        productInfo.put("image", image);

        List<Attribute> attr = new ArrayList<>();
        attr.add(new Attribute(1, "Yellow"));
        productInfo.put("attributes", attr); //        productInfo.put("description", "Pellentesque habitant morbi tristique senectus et netus");
        Map product = wooCommerce.create("products/129/variations", productInfo);
        System.out.println(product);
//        System.out.println(product.get("id"));

        // Get all with request parameters
        Map<String, String> params = new HashMap<>();
        params.put("per_page", "100");
        params.put("offset", "0");
        List products = wooCommerce.getAll(EndpointBaseType.PRODUCTS.getValue(), params);
    }

    static class AttributeOption {

        int id;
        String option;

        public AttributeOption(int id, String option) {
            this.id = id;
            this.option = option;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getOption() {
            return option;
        }

        public void setOption(String option) {
            this.option = option;
        }

    }

    //Multi option        
    static class LocalProductAttribute {

        int id;
        List<String> options;
        boolean visible = true;
        boolean variation = true;

        public LocalProductAttribute(int id, List<String> options) {
            this.id = id;
            this.options = options;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public boolean isVariation() {
            return variation;
        }

        public void setVariation(boolean variation) {
            this.variation = variation;
        }

    }

    static class Image implements Serializable {

        String src;

        public Image(String src) {
            this.src = src;
        }

        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }

    }

    static class Attribute implements Serializable {

        int id;
        String option;

        public Attribute(int id, String option) {
            this.id = id;
            this.option = option;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getOption() {
            return option;
        }

        public void setOption(String option) {
            this.option = option;
        }

    }

    public class CategoryData implements Serializable {

        int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public CategoryData(int id) {
            this.id = id;
        }
    }

    public static void main(String[] args) throws URISyntaxException, IOException, JSONException {
        CrawlerModel.Instance.init();
//        System.out.println(CrawlerModel.COLOR_ID);
        System.out.println(CrawlerModel.COLOR_ID);
        List<Item> items = CrawlerModel.Instance.crawlOneCategory("https://teechip.com/shop/women/tank-tops/unisex-tank");
        CrawlerModel.Instance.pushProductToServer(items, "unisex-tank");

    }
}
