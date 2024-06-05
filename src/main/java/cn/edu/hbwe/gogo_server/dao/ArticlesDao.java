package cn.edu.hbwe.gogo_server.dao;

import cn.edu.hbwe.gogo_server.entity.Articles;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticlesDao extends BaseMapper<Articles> {
}
