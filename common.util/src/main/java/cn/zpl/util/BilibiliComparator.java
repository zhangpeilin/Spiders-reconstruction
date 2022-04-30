package cn.zpl.util;

import java.math.BigDecimal;
import java.util.Comparator;

public class BilibiliComparator implements Comparator<String> {
	
	private boolean asc;
	public BilibiliComparator(boolean asc){
		this.asc = asc;
	}
	
	@Override
	//【ASMR❤欣小萌】录播合集 持续更新 (22859277) p1 order1.flv
	public int compare(String o1, String o2) {
		o1 = o1.replace(".flv", "").split("order")[1];
		o2 = o2.replace(".flv", "").split("order")[1];
		if (asc) {
			return new BigDecimal(o1).compareTo(new BigDecimal(o2));
		}
		return new BigDecimal(o2).compareTo(new BigDecimal(o1));
	}
}