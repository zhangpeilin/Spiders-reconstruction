package cn.zpl.dao.service.impl;

import cn.zpl.common.bean.Token;
import cn.zpl.dao.mapper.TokenMapper;
import cn.zpl.dao.service.ITokenService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zpl
 * @since 2022-08-03
 */
@Service
public class TokenServiceImpl extends ServiceImpl<TokenMapper, Token> implements ITokenService {

}
