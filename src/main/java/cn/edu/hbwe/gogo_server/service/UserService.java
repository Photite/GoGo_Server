package cn.edu.hbwe.gogo_server.service;

import cn.edu.hbwe.gogo_server.dao.UserDao;
import cn.edu.hbwe.gogo_server.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author Photite
 */
@Service
public class UserService {

    // 注入 UserDao
    @Autowired
    private UserDao userDao;

    // 注入 StringRedisTemplate
    @Autowired
    private StringRedisTemplate redisTemplate;

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

    public User findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", username);
        return userDao.selectOne(queryWrapper);
    }

    public void saveToken(String token) {
        redisTemplate.boundValueOps("userToken").set(token);
        redisTemplate.expire("userToken", 7, TimeUnit.DAYS);
    }


}
