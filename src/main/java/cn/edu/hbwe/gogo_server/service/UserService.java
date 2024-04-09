package cn.edu.hbwe.gogo_server.service;

import cn.edu.hbwe.gogo_server.dao.UserDao;
import cn.edu.hbwe.gogo_server.dto.Result;
import cn.edu.hbwe.gogo_server.entity.ClassUnit;
import cn.edu.hbwe.gogo_server.entity.Term;
import cn.edu.hbwe.gogo_server.entity.User;
import cn.edu.hbwe.gogo_server.entity.YearAndSemestersPicker;
import cn.edu.hbwe.gogo_server.exception.LoginException;
import cn.edu.hbwe.gogo_server.utils.EduSystemLoginUtil;
import cn.edu.hbwe.gogo_server.utils.HTTPUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Photite
 */
@Service
public class UserService {

    // 注入 UserDao
    @Autowired
    private UserDao userDao;

    // 登录方法
    public User login(String username, String password) {
        String pwd = DigestUtils.md5Hex(password + username);
        // 使用QueryWrapper构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username).eq("password", pwd);
        // 调用UserDao的selectOne方法来进行用户登录验证
        return userDao.selectOne(queryWrapper);
    }

    // 注册方法
    public boolean register(String username, String password) {
        String pwd = DigestUtils.md5Hex(password + username);
        int i = userDao.insert(new User(username, pwd));
        return i > 0;
    }






}
