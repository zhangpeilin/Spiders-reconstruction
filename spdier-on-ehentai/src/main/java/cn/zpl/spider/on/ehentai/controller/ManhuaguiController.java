package cn.zpl.spider.on.ehentai.controller;

import cn.zpl.pojo.Data;
import cn.zpl.pojo.DownloadDTO;
import cn.zpl.pojo.SynchronizeLock;
import cn.zpl.thread.OneFileOneThread;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.DownloadTools;
import cn.zpl.util.LZString;
import cn.zpl.util.RunJavaScript;
import cn.zpl.util.SaveLogForImages;
import cn.zpl.util.UrlContainer;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.script.ScriptException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


@RestController
@Slf4j
public class ManhuaguiController {


    @GetMapping("/manhuagui/{id}")
    public void domain2(@PathVariable("id") String id) throws UnsupportedEncodingException {
        String page = "https://www.manhuagui.com/comic/" + id;
        Data data = new Data();
        data.setUrl(page);
        data.setProxy(true);
        data.setWaitSeconds(20);
        data.setAlwaysRetry();
        data.setHeader("accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp," +
                "image/apng,*/*;q=0.8,application/signed-exchange;v=b3\n" +
                "accept-encoding: gzip, deflate, br\n" +
                "accept-language: zh-CN,zh;q=0.9,en;q=0.8,ja;q=0.7,zh-TW;q=0.6\n" +
                "cache-control: no-cache\n" +
                "cookie: country=US; _ga=GA1.1.603769205.1705665718; isAdult=1; cookie_url_referrer=https%3a%2f%2fwww.manhuagui.com%2fcomic%2f44821%2f; my=huashengweijian%7c329AA3A5421D7A9DCA5CDC81D45A6953; _ga_H5F270PE29=GS1.1.1705665718.1.1.1705665825.0.0.0\n" +
                "user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
        CommonIOUtils.withTimer(data);
        String pageStr = data.getResult();
        Elements chapters = CommonIOUtils.getElementsFromStr(pageStr, "div.chapter-list a");
        if (chapters.isEmpty()) {
            Element elementFromStr = CommonIOUtils.getElementFromStr(pageStr, "input#__VIEWSTATE");
            System.out.println(elementFromStr.attr("value"));
            String decompress = LZString.decompressFromBase64(elementFromStr.attr("value"));
            if (StringUtils.isEmpty(decompress)) {
                return;
            }
            chapters = CommonIOUtils.getElementsFromStr(decompress, "div.chapter-list a");
        }
        for (Element chapter : chapters) {
            List<DownloadDTO> downloadDTOList = new ArrayList<>();
            chapter.setBaseUri("https://www.manhuagui.com");
            String chapterUrl = chapter.absUrl("href");
            String str;
            do {
                str = getResult(chapterUrl);
            } while (StringUtils.isEmpty(str));
            str = str.substring(str.indexOf("(") + 1, CommonIOUtils.getIndex(str,
                    str.indexOf("SMH.imgData")) - 1);
            JsonElement initData = CommonIOUtils.paraseJsonFromStr(str);
            JsonElement images = CommonIOUtils.getFromJson2(initData, "files");
            String path = CommonIOUtils.getFromJson2Str(initData, "path");
            String cid = CommonIOUtils.getFromJson2Str(initData, "cid");
            String md5 = CommonIOUtils.getFromJson2Str(initData, "sl-md5");
            String bid = CommonIOUtils.getFromJson2Str(initData, "bid");
            String cname = CommonIOUtils.getFromJson2Str(initData, "cname");
            String bname = CommonIOUtils.getFromJson2Str(initData, "bname");
            //https://eu.hamreus.com/ps3/g/gblss_hlhj/%E7%AC%AC01%E5%9B%9E/001.jpg.webp?cid=239686&md5=IOs7Zb0wS16by55LFHKIFA
            URIBuilder builder = new URIBuilder();
            builder.setHost("eu.hamreus.com");
            builder.setScheme("https");
            builder.setPath(path + "001.jpg.webp");
            builder.setParameter("cid", cid);
            builder.setParameter("md5", md5);
            List<String> pathMake = new ArrayList<>();
            pathMake.add("h:\\manhuagui");
            pathMake.add(bname);
            pathMake.add(cname);
            SynchronizeLock lock = new SynchronizeLock();
            if (images.isJsonArray()) {
                for (JsonElement jsonElement : images.getAsJsonArray()) {
                    DownloadDTO dto = new DownloadDTO();
                    builder.setPath(path + URLDecoder.decode(jsonElement.getAsString(), "utf-8"));
                    dto.setUrl(builder.toString());
                    dto.setFileName(jsonElement.getAsString());
                    dto.setSynchronizeLock(lock);
                    dto.setProxy(true);
                    dto.setAlwaysRetry();
                    dto.setReferer("https://www.manhuagui.com/comic/" + bid + "/" + cid + ".html");
                    dto.setSavePath(CommonIOUtils.makeFilePath(pathMake, jsonElement.getAsString().replace(".webp", "")));
//                dto.setFileLength(String.valueOf(URLConnectionTool.getDataLength(dto)));
                    downloadDTOList.add(dto);
                }
            }
            DownloadTools tools = DownloadTools.getInstance(10);
            downloadDTOList.forEach(downloadDTO -> tools.ThreadExecutorAdd(new OneFileOneThread(downloadDTO)));
            tools.shutdown();
            File file = new File(downloadDTOList.get(0).getSavePath());
            SaveLogForImages.saveLog(file.getParentFile());
        }

    }

    public String getResult(String pageUrl) {
        String before = "var LZString=(function(){var f=String.fromCharCode;var keyStrBase64=\"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=\";var baseReverseDic={};function getBaseValue(alphabet,character){if(!baseReverseDic[alphabet]){baseReverseDic[alphabet]={};for(var i=0;i<alphabet.length;i++){baseReverseDic[alphabet][alphabet.charAt(i)]=i}}return baseReverseDic[alphabet][character]}var LZString={decompressFromBase64:function(input){if(input==null)return\"\";if(input==\"\")return null;return LZString._0(input.length,32,function(index){return getBaseValue(keyStrBase64,input.charAt(index))})},_0:function(length,resetValue,getNextValue){var dictionary=[],next,enlargeIn=4,dictSize=4,numBits=3,entry=\"\",result=[],i,w,bits,resb,maxpower,power,c,data={val:getNextValue(0),position:resetValue,index:1};for(i=0;i<3;i+=1){dictionary[i]=i}bits=0;maxpower=Math.pow(2,2);power=1;while(power!=maxpower){resb=data.val&data.position;data.position>>=1;if(data.position==0){data.position=resetValue;data.val=getNextValue(data.index++)}bits|=(resb>0?1:0)*power;power<<=1}switch(next=bits){case 0:bits=0;maxpower=Math.pow(2,8);power=1;while(power!=maxpower){resb=data.val&data.position;data.position>>=1;if(data.position==0){data.position=resetValue;data.val=getNextValue(data.index++)}bits|=(resb>0?1:0)*power;power<<=1}c=f(bits);break;case 1:bits=0;maxpower=Math.pow(2,16);power=1;while(power!=maxpower){resb=data.val&data.position;data.position>>=1;if(data.position==0){data.position=resetValue;data.val=getNextValue(data.index++)}bits|=(resb>0?1:0)*power;power<<=1}c=f(bits);break;case 2:return\"\"}dictionary[3]=c;w=c;result.push(c);while(true){if(data.index>length){return\"\"}bits=0;maxpower=Math.pow(2,numBits);power=1;while(power!=maxpower){resb=data.val&data.position;data.position>>=1;if(data.position==0){data.position=resetValue;data.val=getNextValue(data.index++)}bits|=(resb>0?1:0)*power;power<<=1}switch(c=bits){case 0:bits=0;maxpower=Math.pow(2,8);power=1;while(power!=maxpower){resb=data.val&data.position;data.position>>=1;if(data.position==0){data.position=resetValue;data.val=getNextValue(data.index++)}bits|=(resb>0?1:0)*power;power<<=1}dictionary[dictSize++]=f(bits);c=dictSize-1;enlargeIn--;break;case 1:bits=0;maxpower=Math.pow(2,16);power=1;while(power!=maxpower){resb=data.val&data.position;data.position>>=1;if(data.position==0){data.position=resetValue;data.val=getNextValue(data.index++)}bits|=(resb>0?1:0)*power;power<<=1}dictionary[dictSize++]=f(bits);c=dictSize-1;enlargeIn--;break;case 2:return result.join('')}if(enlargeIn==0){enlargeIn=Math.pow(2,numBits);numBits++}if(dictionary[c]){entry=dictionary[c]}else{if(c===dictSize){entry=w+w.charAt(0)}else{return null}}result.push(entry);dictionary[dictSize++]=w+entry.charAt(0);enlargeIn--;w=entry;if(enlargeIn==0){enlargeIn=Math.pow(2,numBits);numBits++}}}};return LZString})();String.prototype.splic=function(f){return LZString.decompressFromBase64(this).split(f)};\n";
        UrlContainer container = new UrlContainer(pageUrl);
        Data data = new Data();
        data.setAlwaysRetry();
        data.setProxy(true);
        data.setWaitSeconds(10);
        data.setUrl(container.getUrl());
        CommonIOUtils.withTimer(data);
        if (data.getResult() == null) {
            return "";
        }
        Elements scripts = CommonIOUtils.getElementsFromStr(data.getResult(),
                "script");
        String eval = null;
        for (Element script : scripts) {
            if (script.data().contains("\\x65\\x76\\x61\\x6c")) {
                System.out.println();
                eval = script.data().replace("window[\"\\x65\\x76\\x61\\x6c\"](", "var result = ");
                eval = eval.substring(0, eval.length() - 2);
                break;
            }
        }
        try {
            eval = (String) RunJavaScript.executeForAttribute(before + eval, "result");
            if (eval != null) {
                return eval;
            }
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return "";
    }
}


