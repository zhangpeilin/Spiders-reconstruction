package cn.zpl.tencent.config;

import cn.zpl.common.bean.Cron;
import cn.zpl.util.CrudTools;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Configuration
@EnableScheduling
public class DynamicScheduleTask implements SchedulingConfigurer {

    @Resource
    CrudTools tools;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(() -> System.out.println("执行动态定时任务：" + LocalDateTime.now().toLocalTime()), triggerContext -> {
            List<Cron> result = tools.commonApiQueryBySql("sql:select cron from cron limit 1", Cron.class);
            String cron = result.get(0).getCron();
            if (StringUtils.isEmpty(cron)) {
                return new Date();
            }
            return new CronTrigger(cron).nextExecutionTime(triggerContext);
        });
    }
}
