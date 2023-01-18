package cn.zpl.dao.service.impl;

import cn.zpl.common.bean.Cron;
import cn.zpl.dao.mapper.CronMapper;
import cn.zpl.dao.service.ICronService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zpl
 * @since 2022-08-25
 */
@Service
public class CronServiceImpl extends ServiceImpl<CronMapper, Cron> implements ICronService {

}
