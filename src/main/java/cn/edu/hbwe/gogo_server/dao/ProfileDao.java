package cn.edu.hbwe.gogo_server.dao;

import cn.edu.hbwe.gogo_server.entity.Profile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Photite
 */
@Mapper
public interface ProfileDao extends BaseMapper<Profile> {
}

