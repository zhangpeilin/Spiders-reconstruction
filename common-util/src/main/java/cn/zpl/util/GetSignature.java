package cn.zpl.util;


import cn.zpl.thirdParty.HMACSHA256;

public class GetSignature {

    private static String secret = "~d}$Q7$eIni=V)9\\RK/P.RM4;9[7|@/CA}b~OW!3?EV`:<>M7pddUBL5n|0/*Cn";
    private static String random = "C69BAF41DA5ABD1FFEDC6D2FEA56B";
    public static void main(String[] args) {
        System.out.println(generateSignature("auth/sign-in", "1566301708",
                "46e4fff187504dbcac7936d33b45ed49", "POST"));
//        System.out.println(HMACSHA256.sha256_HMAC("auth/sign" +
//                "-in1564829826d0f4401b8f054b0ba54ca443156e0104POSTC69BAF41DA5ABD1FFEDC6D2FEA56B", secret));
    }

    public static String generateSignature(String path, String systemTime, String uuid, String method){
        String[] list = {path, systemTime, uuid, method, random};


        return HMACSHA256.sha256_HMAC((path + systemTime + uuid + method + random).toLowerCase(), secret);
    }
}
