/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crawler.teachip.common;

import com.crawler.teechip.entity.Category;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
public class JSoupUtils {

    public static final String MAIN_URL = "https://teechip.com";

    public static List<Category> getAllCategory() throws IOException {
        Document doc = Jsoup.connect(MAIN_URL).get();
        List<Category> categories = new ArrayList<>();
        Elements categoryEle = doc.select(".w-container.z-0").select(".hover-trigger");
        for (Element ele : categoryEle) {
            Element parent = ele.selectFirst(".d-ib.px-1.self-center.py-p5.fw-bold");
            String title = parent.text();
            String link = parent.attr("href");
            Category category = new Category(title, link, true);
            Elements subElementWrappers = ele.select(".d-ib.mx-1.my-p75");
            for (Element subEleWrap : subElementWrappers) {
                Elements subElements = subEleWrap.select("a");
                int count = 0;
                boolean flag = false;
                Category subCategory = null;
                if (subElements.size() > 1) {
                    flag = true;
                }
                for (Element subElement : subElements) {
                    String name = subElement.text();
                    String sublink = subElement.attr("href");
                    if (subElement.hasClass("mb-1")) {
                        subCategory = new Category(name, sublink, false);
                        category.addSubCategory(subCategory);
                        continue;
                    }
                    if (count == 0) {
                        subCategory = new Category(name, sublink, flag);
                    } else {
                        Category subsubCategory = new Category(name, sublink, false);
                        subCategory.addSubCategory(subsubCategory);
                    }
                    count++;
                }

                if (subCategory != null && subCategory.getSubCategories() != null) {
                    category.addSubCategory(subCategory);
                }

            }
            categories.add(category);
        }
        return categories;
    }

    public static String getGroupCode() throws IOException, JSONException {
        Document doc = Jsoup.connect(MAIN_URL).get();
        Element script = doc.selectFirst("body").selectFirst("script");
        
        String mainScript = script.html();
        int start = mainScript.indexOf("{");
        int end = mainScript.lastIndexOf("}");
        String dataJson = mainScript.substring(start, end + 1);
        JSONObject json = new JSONObject(dataJson);
        String groupId = json.getJSONObject("retailCart").getString("groupId");
        return groupId;
    }
    
    public static JSONObject getScriptData(String url) throws JSONException, IOException {
        Document doc = Jsoup.connect(url).get();
        Element script = doc.selectFirst("body").selectFirst("script");
        String mainScript = script.html();
        int start = mainScript.indexOf("{");
        int end = mainScript.lastIndexOf("}");
        String dataJson = mainScript.substring(start, end + 1);
        JSONObject json = new JSONObject(dataJson);
        return json;
    }

    public static void main(String[] args) throws IOException, JSONException {
        System.out.println(getGroupCode());

    }
}
