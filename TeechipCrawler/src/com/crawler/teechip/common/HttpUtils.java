/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crawler.teachip.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author linhhd
 */
public class HttpUtils {
    public static String parseResultFromResponseHttpClient(HttpResponse resp) throws IOException {
		StringBuffer result = new StringBuffer();
		BufferedReader rd = null;
		InputStreamReader input= null;
		try {
			input = new InputStreamReader(resp.getEntity().getContent());
			rd = new BufferedReader(input);
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (UnsupportedOperationException ex) {
			ex.printStackTrace();
		}finally{
			rd.close();
			input.close();
			EntityUtils.consume(resp.getEntity());
		}

		return result.toString();
	}
}
