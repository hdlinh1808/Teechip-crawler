/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crawler.teechip.entity;

import java.io.Serializable;

/**
 *
 * @author linhhd
 */
public class CategoryData implements Serializable{
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
