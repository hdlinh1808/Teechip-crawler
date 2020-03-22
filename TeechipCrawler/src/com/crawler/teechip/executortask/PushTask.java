/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crawler.teechip.executortask;

import com.crawler.teechip.entity.Item;
import com.crawler.teechip.model.CrawlerTaskModel;

/**
 *
 * @author linhhd
 */
public class PushTask implements Runnable{
        
        Item item;
        String category;
        CrawlerTaskModel model;

        public PushTask(Item item, String category, CrawlerTaskModel model) {
            this.item = item;
            this.category = category;
            this.model = model;
        }

        @Override
        public void run() {
            model.pushProductToServer(item, category);
        }
    }
