package cn.zpl.commondaocenter.service.impl;

import cn.zpl.common.bean.BikaDownloadFailed;
import cn.zpl.commondaocenter.mapper.BikaDownloadFailedMapper;
import cn.zpl.commondaocenter.service.IBikaDownloadFailedService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * bika下载失败记录表 服务实现类
 * </p>
 *
 * @author zpl
 * @since 2022-07-06
 */
@Service
public class BikaDownloadFailedServiceImpl extends ServiceImpl<BikaDownloadFailedMapper, BikaDownloadFailed> implements IBikaDownloadFailedService {

}
