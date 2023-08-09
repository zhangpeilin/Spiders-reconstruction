package cn.zpl;

import cn.zpl.util.CommonStringUtil;

public class TestStringSimulate {
    public static void main(String[] args) {
        String str1 = "[Maita Keikaku (Sennomori Maitake)] Kaikan Mesu Ochi 2 ~Yokubou no Mama Ochi Tsuzukeru Shiori~ | 快感♀堕落2 ～忠实于欲望持续堕落的汐莉～ [Chinese] [瑞树汉化组] [Digital]\n" +
                "[Maita Keikaku (Sennomori Maitake)] Kaikan Mesu Ochi 2 ~Yokubou no Mama Ochi Tsuzukeru Shiori~ 快感♀堕落2 ～忠实于欲望持续堕落的汐莉～ [Chinese] [瑞树汉化组] [Digital]";
        String[] split = str1.split("\n");
        System.out.println(CommonStringUtil.stickCheck(split[0], split[1]));
    }
}
