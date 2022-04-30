package cn.zpl.util;

import java.io.File;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.regex.Pattern;

public class XDFComparator implements Comparator<String> {

	public boolean asc;
	private Pattern pattern = Pattern.compile("-\\d+\\.ts");
	public XDFComparator(boolean asc){
		this.asc = asc;
	}
	
	@Override
	//E:\m3u8\temp\(171804)测试\0-19.ts
	public int compare(String o1, String o2) {
		o1 = pattern.matcher(new File(o1).getName()).replaceAll("");
		o2 = pattern.matcher(new File(o2).getName()).replaceAll("");
		if (asc) {
			return new BigDecimal(o1).compareTo(new BigDecimal(o2));
		}
		return new BigDecimal(o2).compareTo(new BigDecimal(o1));
	}
}