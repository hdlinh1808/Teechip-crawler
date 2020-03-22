/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crawler.teechip.model;

/**
 *
 * @author linhhd
 */
public class Test {

    public static int value = 1;

    public static void main(String[] args) throws InterruptedException {

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (value == 1) {
//                    System.out.println("fasdf");
                }

                System.out.println("1111");

            }
        };

        thread.start();
        Thread.sleep(1000);
        value = 2;
    }

}
