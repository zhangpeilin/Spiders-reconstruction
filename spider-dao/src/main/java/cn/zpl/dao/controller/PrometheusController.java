package cn.zpl.dao.controller;

import cn.zpl.common.bean.RestResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author zhangpl1
 * @date 2022/9/22
 */
@RestController
@RequestMapping("/prometheus/")
@Slf4j
public class PrometheusController {


    @Resource
    MeterRegistry meterRegistry;

    private Counter counter_core;
    private Counter counter_index;
    private AtomicInteger app_online_count;

    @PostConstruct
    private void init(){
        counter_core = meterRegistry.counter("app_requests_method_count", "method", "PrometheusController.core");
        counter_index = meterRegistry.counter("app_requests_method_count", "method", "PrometheusController.index");
        app_online_count = meterRegistry.gauge("app_online_count", new AtomicInteger(0));
    }

    @GetMapping("/testIsUseAble")
    public RestResponse testIsUseAble(){
        counter_index.increment();
        return RestResponse.ok(counter_index.count() + "index of springboot_prometheus.");
    }

    @RequestMapping("testIsCore")
    public RestResponse testIsCore(){
        counter_core.increment();
        return RestResponse.ok(counter_core.count() + "index of spring_boot-prometheus.");
    }

    @RequestMapping("/online")
    public Object online(){
        int people = 0;
        try {
            people = new Random().nextInt(2000);
            app_online_count.set(people);
        } catch (Exception e) {
            return e;
        }
        return "current_online people:" + people;
    }
}
