/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crawler.teechip.entity;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author linhhd
 */
public class Category {
    private String name;
    private String groupId;
    private String crawlLink;
    private String relativeLink;
    private List<Category> subCategories;
    
    public Category(String name, boolean subCateogory){
        this.name = name;
        if(subCateogory){
            this.subCategories = new ArrayList<>();
        }
    }
    
    public Category(String name, String relativeLink,boolean subCateogory){
        this.name = name;
        this.relativeLink = relativeLink;
        if(subCateogory){
            this.subCategories = new ArrayList<>();
        }
    }
    
    
    public boolean addSubCategory(Category subCategory){
        if(subCategories == null){
            return false;
        }
        
        return subCategories.add(subCategory);
    }

    @Override
    public String toString() {
        return "Category{" + "name=" + name + ", groupId=" + groupId + ", relativeLink=" + relativeLink + ", subCategory=" + subCategories + '}' +"\n";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getCrawlLink() {
        return crawlLink;
    }

    public void setCrawlLink(String crawlLink) {
        this.crawlLink = crawlLink;
    }

    public String getRelativeLink() {
        return relativeLink;
    }

    public void setRelativeLink(String relativeLink) {
        this.relativeLink = relativeLink;
    }

    public List<Category> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<Category> subCategories) {
        this.subCategories = subCategories;
    }
}
