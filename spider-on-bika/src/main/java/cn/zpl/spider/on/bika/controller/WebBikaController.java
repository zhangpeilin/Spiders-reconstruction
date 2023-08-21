package cn.zpl.spider.on.bika.controller;

import cn.zpl.common.bean.Bika;
import cn.zpl.spider.on.bika.utils.BikaUtils;
import cn.zpl.util.CrudTools;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
public class WebBikaController {

    private static final List<String> content = new ArrayList<>();
    @Resource
    BikaUtils utils;
    @Resource
    CrudTools tools;

//    @RequestMapping("/getList")
//    public String getList(HttpSession session, Model model, QueryDTO queryDTO) {
//        Data data = new Data();
//        data.setUrl("http://localhost:8080/testMyBatisPlus");
////        Map<String, String> params = new HashMap<>();
////        params.put("query", queryDTO.toString());
////        data.setValuePairs(params);
////        String result = CommonIOUtils.postRestful(data);
//        String jsonString = JSONObject.toJSONString(queryDTO);
//        data.setParams(jsonString);
//        data.setHeader("Content-Type: application/json");
//        CommonIOUtils.postUrl(data);
//        JsonElement element = CommonIOUtils.getFromJson2(data.getResult(), "list");
//        List<Bika> list = JSONObject.parseArray(element.toString(), Bika.class);
//        model.addAttribute("list", list);
//        queryDTO.toMap().forEach(session::setAttribute);
//
//        return "list";
//    }


    @RequestMapping("/search2")
    public String search2() {
        return "search2";
    }

    @RequestMapping("/bika/search")
    public String BikaSearch() {
        return "/bika/search";
    }

    @RequestMapping("/download")
    public String downloadById(@RequestParam("id") String id) {
//        BikaSearch.downloadById(id);
        return "/bika/downloadList";
    }

    @RequestMapping("/download/{id}")
    public String download(@PathVariable("id") String id, @NotNull Model model) {
        List<Bika> downloadArrays = new ArrayList<>();
//        downloadArrays.add(BikaSearch.downloadById(id));
        model.addAttribute("list", downloadArrays);
        return "list";
    }

//    @ResponseBody
//    @RequestMapping("/progress")
//    public Integer getProgress(@RequestParam("id") String id) {
//        return BikaUtils.progress.get(id).get();
//    }

//    @GetMapping("/updateList")
//    public void updateList() {
//        new Thread(() -> ScanComics.main(null)).start();
//    }

//    @GetMapping("/download")
//    public void download() {
//        new Thread(() -> UpdateComics.main(null)).start();
//    }

    /**
     * 用来标记漫画完结或删除
     * @param model
     * @param comicId
     * @return
     */
    @GetMapping("/update/{id}")
    public String update(Model model, @PathVariable("id") String comicId) {
        Bika bika = utils.getBikaExist(comicId);
        model.addAttribute("types", Arrays.asList("del", "ntr", "纯爱", "调教", "其他", "伪娘", "性转"));
        model.addAttribute("bika", bika);
        model.addAttribute("comicId", comicId);
        return "markComplete";
    }

    /**
     * 用来更新指定漫画
     * @param model
     * @param comicId
     * @return
     */
    @GetMapping("/updatenow/{id}")
    public String updateNow(Model model, @PathVariable("id") String comicId) {
        Bika bika = utils.getBikaExist(comicId);
//        BikaUtils.saveBika(bika);
        return "redirect:/comic/{id}";
    }

    @PostMapping("/mark/{id}")
    public String markCompleted(@CookieValue(value = "userToken", required = false) Cookie cookie, @PathVariable("id") String id, boolean level, String type, boolean needDel, RedirectAttributes redirectAttributes) {
        String des = "G:\\Bika完结\\";
        if (level) {
            des += "精品\\";
            des += type;
        }
        if (needDel) {
            des = "G:\\Bika完结\\del";
        }
        System.out.println(des);
//        if (!BikaUtils.moveAndUpdateStatus(id, des)) {
//            return "bika_error";
//        }
        if (cookie != null) {
            String value = cookie.getValue();
//            if (BikaUtils.cache.get(value) instanceof QueryDTO) {
//                redirectAttributes.addFlashAttribute(BikaUtils.cache.get(value));
//                return "redirect:/getList";
//            }
        }
        return "forward:/search";
    }

    //    @GetMapping("/show")
//    public String showWithPages(Model model, HttpSession session) {
//        QueryDTO queryDTO = new QueryDTO();
//        queryDTO.setPages(20);
//        queryDTO.setTotal(5000);
//        queryDTO.setTags("ntr");
//        queryDTO.setCurrent(1);
//        queryDTO.setSize(5);
//        Data data = new Data();
//        data.setUrl("http://localhost:8080/testMyBatisPlus");
//        String jsonString = JSONObject.toJSONString(queryDTO);
//        data.setParams(jsonString);
//        data.setHeader("Content-Type: application/json");
//        CommonIOUtils.postUrl(data);
//        RestResponseBean restResponseBean = JSONObject.parseObject(data.getResult(), RestResponseBean.class);
//        if (!restResponseBean.isSuccess()) {
//            return "redirect:/search";
//        }
//        List<Bika> list = restResponseBean.getList();
//        model.addAttribute("list", list);
//        model.addAttribute("/query", queryDTO);
//        model.addAttribute("pages", restResponseBean.getPages());
//        model.addAttribute("total", restResponseBean.getTotal());
//        queryDTO.toMap().forEach(session::setAttribute);
//        return "dynamic_table";
//    }



//    @GetMapping("/chat")
//    public String chatRoom(Model model) {
//        model.addAttribute("context", content);
//        return "chat";
//    }
//
//    @PostMapping("/say")
//    public String postText(@RequestParam("word") String word) {
//        content.add(word);
//
//        return "redirect:/chat";
//    }
//
//    @GetMapping("/clear")
//    public String clearMsg() {
//        content.clear();
//        return "redirect:/chat";
//    }
}
