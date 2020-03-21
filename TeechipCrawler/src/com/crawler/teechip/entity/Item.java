/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crawler.teechip.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author linhhd
 */
public class Item {

    public static class Size {

        private String size;
        private int price;

        public Size(String size, int price) {
            this.size = size;
            this.price = price;
        }

        public String getSize() {
            return size;
        }

        public int getPrice() {
            return price;
        }

        @Override
        public String toString() {
            return "Size{" + "size=" + size + ", price=" + price + '}';
        }
    }

    public static class Relation {

        private String color;
        private String code;

        public Relation(String color, String code) {
            this.color = color;
            this.code = code;
        }

        public String getColor() {
            return color;
        }

        public String getCode() {
            return code;
        }

        @Override
        public String toString() {
            return "Relation{" + "color=" + color + ", code=" + code + '}';
        }
    }

    private int price;
    private String product;
    private String productId;
    private String design;
    private String[] imageUrl;
    private String color;
    private String code;
    private String campaignUrl;
    private List<Size> sizes;
    private List<Relation> relations;

    public Item(int price, String product, String design) {
        this.price = price;
        this.product = product;
        this.design = design;
    }

    public String[] getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String[] imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getDesign() {
        return design;
    }

    public void setDesign(String design) {
        this.design = design;
    }

    public String getCampaignUrl() {
        return campaignUrl;
    }

    public void setCampaignUrl(String campaignUrl) {
        this.campaignUrl = campaignUrl;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public List<Size> getSizes() {
        return sizes;
    }

    public void setSizes(List<Size> sizes) {
        this.sizes = sizes;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }

    @Override
    public String toString() {
        return "\nItem{" + "color=" + color + ", code=" + code + ", sizes=" + sizes + ", relations=" + relations + '}';
    }

    public List<String> getAllSize() {
        List<String> arr = new ArrayList();
        for (Size size : sizes) {
            arr.add(size.size);
        }
        return arr;
    }
    
    public List<String> getAllColor() {
        List<String> arr = new ArrayList();
        for (Relation relation : relations) {
            arr.add(relation.color);
        }
        return arr;
    }

    public String toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", this.product);
        json.put("regular_price", String.valueOf(sizes.get(0).getPrice()));
        JSONArray images = new JSONArray();
        for (String url : imageUrl) {
            JSONObject jImage = new JSONObject();
            jImage.put("src", url);
            images.put(jImage);
        }
        json.put("images", images);

        return json.toString();
    }

}
