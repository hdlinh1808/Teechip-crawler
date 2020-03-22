/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crawler.teechip.entity;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author linhhd
 */

// multi option
public class LocalProductAttribute implements Serializable{
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
