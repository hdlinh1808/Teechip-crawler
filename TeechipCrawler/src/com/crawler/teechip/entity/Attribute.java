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
public class Attribute implements Serializable{

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
