package cn.edu.hbwe.gogo_server.service;

import cn.edu.hbwe.gogo_server.dao.UserDao;
import cn.edu.hbwe.gogo_server.entity.User;
import cn.edu.hbwe.gogo_server.entity.WeChatSession;
import cn.edu.hbwe.gogo_server.utils.EduSystemLoginUtil;
import cn.edu.hbwe.gogo_server.utils.HTTPUtil;
import cn.edu.hbwe.gogo_server.utils.JWTUtils;
import cn.edu.hbwe.gogo_server.utils.WXUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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

    @Autowired
    private WXUtil wxUtil;

    // 引入Log4j2日志 日志记录器
    private static final Logger logger = LogManager.getLogger(EduSystemLoginUtil.class);

    @Value("${wx.AppId}")
    private String appid;

    @Value("${wx.appSecret}")
    private String appsecret;

    // 用于存储用户信息和token
    Map<String, Object> map = new HashMap<>();

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

    // 根据openid查询用户eduUsername
    public String findEduUsernameByOpenid(String openid) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("open_id", openid);
        User user = userDao.selectOne(queryWrapper);
        return user.getEduUsername();
    }

    // 根据eduUsername查询openid
    public String findOpenidByEduUsername(String eduUsername) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("edu_username", eduUsername);
        User user = userDao.selectOne(queryWrapper);
        return user.getOpenId();
    }

    public Map<String, Object> Login(String code, String eduUsername, String eduPassword) throws Exception {
        // 根据传入code，调用微信服务器，获取唯一openid
        // 微信服务器接口地址
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appid + "&secret=" + appsecret
                + "&js_code=" + code + "&grant_type=authorization_code";
        String errmsg = "";
        String errcode = "";
        String session_key = "";
        String openid = "";
        WeChatSession weChatSession = null;
        // 发送请求
        // 使用HTTPUtil发送GET请求
        System.out.println(url);
        Connection.Response response = HTTPUtil.sendGetRequest(url, HTTPUtil.createCommonHeaders(), null);
        System.out.println(111111);
        // 判断请求是否成功
        if (response != null && response.statusCode() == HttpStatus.OK.value()) {
            // 获取主要内容
            String sessionData = response.body();
            //将json字符串转化为实体类;
            weChatSession = JSON.parseObject(sessionData, WeChatSession.class);
            logger.info("返回的数据==>{}", weChatSession);
            //获取用户的唯一标识openid
            openid = weChatSession.getOpenid();
            //获取错误码
            errcode = weChatSession.getErrcode();
            //获取错误信息
            errmsg = weChatSession.getErrmsg();
        } else {
            logger.info("出现错误，错误信息：{}", errmsg);
            map.put("errmsg", errmsg);
            return map;
        }
        // 判断是否成功获取到openid
        if ("".equals(openid) || openid == null) {
            logger.info("错误获取openid,错误信息:{}", errmsg);
            map.put("errmsg", errmsg);
            return map;
        } else {
            // 判断用户是否存在，查询数据库
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("open_id", openid);
            User user = userDao.selectOne(queryWrapper);
            // 不存在，加入数据表
            if (user == null) {
                // 填充初始信息
                User tempUser = new User("", "", openid, eduUsername, eduPassword);
                // 加入数据表
                userDao.insert(tempUser);
                // 加入map返回
                map.put("user", tempUser);
                // 调用自定义类封装的方法，创建token
                String token = JWTUtils.createToken(tempUser.getId().toString());
                map.put("token", token);
                return map;
            } else {
                // 存在，将用户信息加入map返回
                map.put("user", user);
                String token = JWTUtils.createToken(user.getId().toString());
                map.put("token", token);
                return map;
            }
        }
    }


}
