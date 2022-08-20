package cn.zpl.commondaocenter.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public interface OriSqlMapper extends BaseMapper<LinkedList<Object>>  {

    LinkedList<Object> OriSql(QueryWrapper<Object> queryWrapper);
}
