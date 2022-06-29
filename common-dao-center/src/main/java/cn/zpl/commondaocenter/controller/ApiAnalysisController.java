package cn.zpl.commondaocenter.controller;

import cn.zpl.common.bean.RestResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("common/dao")
public class ApiAnalysisController {

    //路径规则：entity代表要查询的实体对象
    ///api/mofdiv?size=999999&fetchProperties=*,parent[id,name,code,lastModifiedVersion]&sort=code,asc
    @GetMapping("/api/{entity}")
    public RestResponse apiAnalysis(@PathVariable("entity") String entity, int size, @RequestParam("fetchProperties") List<String> fetchProperties, @RequestParam("sort")List<String> sort){
        System.out.println(entity);
        System.out.println(size);
        System.out.println(fetchProperties);
        System.out.println(sort);
        return RestResponse.ok();

    }
}
