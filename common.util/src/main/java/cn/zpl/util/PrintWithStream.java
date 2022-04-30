package cn.zpl.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PrintWithStream extends Thread {
	InputStream is = null;

	public PrintWithStream(InputStream is) {
		this.is = is;
	}

	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					"utf-8"));
			String temp = "";
			StringBuffer str = new StringBuffer();
			while ((temp = br.readLine()) != null) {
				str.append(temp).append("\r\n");
				System.out.println(temp);
			}
			System.out.println("读取终止");
			is.close();
			br.close();
			// System.out.println(str);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}