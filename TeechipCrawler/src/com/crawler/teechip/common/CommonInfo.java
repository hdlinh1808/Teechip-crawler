/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crawler.teechip.common;

import com.icoderman.woocommerce.oauth.OAuthConfig;

/**
 *
 * @author linhhd
 */
public class CommonInfo {
    
    public static CommonInfo Instance = new CommonInfo();
    private String myUrl;
    private String consumerKey;
    private String consumerSecret;
    
//    private OAuthConfig config = new OAuthConfig("http://localhost/duydl/index.php",
//            "ck_6c31e47de04da961391e80048077d86d9288c4b2",
//            "cs_c89f2085df2f1acbb3d87f7e8f9b7495791d3cbd");
    
    private CommonInfo(){
        
    }

    public void setConfig(String url, String consumerKey, String consumerSecret) {
//        this.config = config;
    }
}
