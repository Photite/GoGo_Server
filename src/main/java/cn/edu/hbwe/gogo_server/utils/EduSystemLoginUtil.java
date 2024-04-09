package cn.edu.hbwe.gogo_server.utils;

import cn.edu.hbwe.gogo_server.dao.UserDao;
import cn.edu.hbwe.gogo_server.entity.LoginAuthorization;
import cn.edu.hbwe.gogo_server.entity.User;
import cn.edu.hbwe.gogo_server.exception.LoginException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 教务系统登录工具类
 *
 * @author Photite
 */
@Component
public class EduSystemLoginUtil {

    // 引入Log4j2日志 日志记录器
    private static final Logger logger = LogManager.getLogger(EduSystemLoginUtil.class);

    // 引入StringRedisTemplate类实例
    @Autowired
    private StringRedisTemplate redisTemplate;

    // 引入LoginAuthorization类实例
    private final LoginAuthorization auth = new LoginAuthorization();

    // 引入UserDao类实例
    @Autowired
    private UserDao userDao;

    public Map<String, String> getCookies(String eduUsername) {
        try {
            // 从Redis中获取cookie
            String cookieJson = redisTemplate.boundValueOps(eduUsername).get();
            // 如果cookie不存在或无效，重新获取并存储到Redis中
            if (cookieJson == null || cookieJson.isEmpty()) {
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("edu_username", eduUsername);
                // 调用UserDao的selectOne方法来进行用户登录验证
                User user = userDao.selectOne(queryWrapper);
                System.out.println("Redis当中不存在cookie，尝试登录获取");
                // 重新获取cookie的逻辑，这里假设是通过一个名为getNewCookie的函数来获取
                Map<String, String> newCookies = loginAndGetCookies(eduUsername, user.getEduPassword());
                System.out.println("新的cookies: " + newCookies);
                // 创建ObjectMapper对象
                ObjectMapper mapper = new ObjectMapper();
                // 将新的cookies转换为JSON字符串
                String newCookieJson = mapper.writeValueAsString(newCookies);
                System.out.println("新的cookieJson: " + newCookieJson);
                // 将新的cookie存储到Redis中
                redisTemplate.boundValueOps(eduUsername).set(newCookieJson);
                redisTemplate.expire(eduUsername, 7, TimeUnit.HOURS);
                // 返回新的cookies
                return newCookies;
            } else {
                // 创建ObjectMapper对象
                ObjectMapper mapper = new ObjectMapper();
                // 将JSON字符串转换为Map
                return mapper.readValue(cookieJson, new TypeReference<>() {
                });
            }
        } catch (LoginException e) {
            logger.error("登录失败，没有获取到Cookie数据");
            throw new LoginException(e.getCode(), e.getMsg());
        } catch (Exception e) {
            logger.error("登录过程存在错误");
            throw new LoginException("登录出错，请稍后再试");
        }
    }

    public Map<String, String> loginAndGetCookies(String eduUsername, String eduPassword) throws Exception {

        System.out.println("stuNum: " + eduUsername + "password: " + eduPassword);
        // 调用init方法，并使用LoginAuthorization对象存储数据
        System.out.println("登录请求已发送1");
        init(auth);
        System.out.println("登录请求已发送2");
        // 加密密码并使用LoginAuthorization对象中的公钥信息
        eduPassword = RSAEncoder.encrypt(eduPassword, B64.b64tohex(auth.getPublicKey().get("modulus")), B64.b64tohex(auth.getPublicKey().get("exponent")));
        System.out.println("登录请求已发送3");
        eduPassword = B64.hex2b64(eduPassword);
        System.out.println("登录请求已发送4");
        // 创建请求头和请求数据
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        Map<String, String> data = new HashMap<>();
        data.put("csrftoken", auth.getCsrf());
        data.put("yhm", eduUsername);
        data.put("mm", eduPassword);

        // 使用HTTPUtil发送POST请求，传递LoginAuthorization的cookies
        Connection.Response response = HTTPUtil.sendPostRequest("/jwglxt/xtgl/login_slogin.html", headers, data, auth.getCookies());
        // 更新登录成功后的cookies
        auth.getCookies().put("JSESSIONID", response.cookie("JSESSIONID"));
        Document document = Jsoup.parse(response.body());
        System.out.println("登录请求已发送5");
        // 判断是否登录成功
        if (document.getElementById("tips") == null) {
            System.out.println("登录成功");
            System.out.println("cookies: " + auth.getCookies());
            return auth.getCookies();
        } else {
            logger.error("用户名或密码错误，登录失败");
            throw new LoginException("2001", "用户名或密码错误");
        }
    }

    public boolean isCookieValid(String eduUsername) {
        try {
            Map<String, String> cookies = getCookies(eduUsername);
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
            Connection.Response response = HTTPUtil.sendGetRequest("/jwglxt/xsxy/xsxyqk_cxXsxyqkIndex.html?gnmkdm=N105515&layout=default&su=" + eduUsername, headers, cookies);
            String body = response.body();
            assertLogin(Jsoup.parse(body));
        } catch (LoginException e) {
            logger.error("Cookie已过期", e);
            throw new LoginException("2002", e.getMsg());
        } catch (Exception e) {
            logger.error("Cookie验证失败", e);
            throw new LoginException("2003", "Cookie验证失败");
        }
        return true;
    }

    public void assertLogin(Document doc) {
        for (Element e : doc.getElementsByTag("h5")) {
            if ("用户登录".equals(e.text())) {
                logger.error("存在用户登录提示，Cookie失效");
                throw new LoginException("Cookie失效，请尝试重新获取信息!");
            }
        }
    }

    public void init(LoginAuthorization auth) {
        // 确保auth不为空并且其cookies已初始化
        if (auth != null && auth.getCookies() != null) {
            try {
                System.out.println("初始化已开始");
                getCsrftoken(auth);
                getPublickey(auth);
                System.out.println("初始化已完成");
            } catch (Exception e) {
                logger.error("初始化期间出错", e);
                // 清除已获取的数据，避免使用到错误的数据
                auth.setCsrf(null);
                auth.getPublicKey().clear();
            }
        } else {
            throw new IllegalArgumentException("LoginAuthorization 实例或其 cookie 不得为 null");
        }
    }

    // 修改获取csrftoken和Cookies的方法
    private void getCsrftoken(LoginAuthorization auth) {
        try {
            System.out.println("获取csrftoken");
            // 创建请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0");
            System.out.println("原有的cookies: " + auth.getCookies());
            auth.getCookies().clear();
            // 使用HTTPUtil发送GET请求
            Connection.Response response = HTTPUtil.sendGetRequest("/jwglxt/xtgl/login_slogin.html?language=zh_CN&_t=" + new Date().getTime(), headers, auth.getCookies());
            auth.getCookies().put("JSESSIONID", response.cookie("JSESSIONID"));
            auth.getCookies().put("route", response.cookie("route"));
            Document document = Jsoup.parse(response.body());
            auth.setCsrf(Objects.requireNonNull(document.getElementById("csrftoken")).val());
            System.out.println(auth.getCsrf());
        } catch (Exception e) {
            logger.error("无法获取 csrftoken", e);
        }
    }

    // 获取RSA公钥的方法
    private void getPublickey(LoginAuthorization auth) {
        try {
            System.out.println("获取RSA公钥");
            // 创建请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0");
            System.out.println("获取csrf后的cookies: " + auth.getCookies());
            // 使用HTTPUtil发送GET请求
            Connection.Response response = HTTPUtil.sendGetRequest("/jwglxt/xtgl/login_getPublicKey.html?" +
                    "time=" + new Date().getTime(), headers, auth.getCookies());
            JSONObject jsonObject = JSON.parseObject(response.body());
            auth.getPublicKey().put("modulus", jsonObject.getString("modulus"));
            auth.getPublicKey().put("exponent", jsonObject.getString("exponent"));
            System.out.println(auth.getPublicKey().get("modulus"));
            System.out.println(auth.getPublicKey().get("exponent"));
        } catch (Exception e) {
            logger.error("无法获取 RSA 公钥", e);
        }
    }
}
