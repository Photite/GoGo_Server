package cn.edu.hbwe.gogo_server.service;

import cn.edu.hbwe.gogo_server.dto.Result;
import cn.edu.hbwe.gogo_server.entity.Term;
import cn.edu.hbwe.gogo_server.entity.YearAndSemestersPicker;
import cn.edu.hbwe.gogo_server.exception.LoginException;
import cn.edu.hbwe.gogo_server.utils.EduSystemLoginUtil;
import cn.edu.hbwe.gogo_server.utils.HTTPUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Photite
 */
@Service
public class EduService {

    // 引入Log4j2日志 日志记录器
    private static final Logger logger = LogManager.getLogger(EduSystemLoginUtil.class);

    // 引入StringRedisTemplate类实例
    @Autowired
    private StringRedisTemplate redisTemplate;

    //创建EduSystemLoginUtil实例
    @Autowired
    private EduSystemLoginUtil eduSystemLoginUtil;

    // 定义一个模拟登录教务系统的方法，接收学号和密码，返回Result
    public Result eduLogin(String eduUsername, String eduPassword) {
        try {
            Map<String, String> cookies = eduSystemLoginUtil.loginAndGetCookies(eduUsername, eduPassword);
            // 创建ObjectMapper对象
            ObjectMapper mapper = new ObjectMapper();
            // 将新的cookies转换为JSON字符串
            String newCookieJson = mapper.writeValueAsString(cookies);
            // 将新的cookie存储到Redis中
            redisTemplate.boundValueOps(eduUsername).set(newCookieJson);
            redisTemplate.expire(eduUsername, 24, TimeUnit.HOURS);
            return new Result("登录成功", "1000", null);
        } catch (SocketTimeoutException e) {
            throw new LoginException("服务器响应时间过长，请稍后再试！！！");
        } catch (NullPointerException e) {
            throw new LoginException("登录失败，请检查教务系统账号密码是否进行绑定");
        } catch (Exception e) {
            logger.error("登录失败", e);
            throw new LoginException(e.getMessage());
        }
    }

    // 定义一个获取学期的方法，接收学号，返回YearAndSemestersPicker
    public YearAndSemestersPicker getPicker(String eduUsername) {
        try {
            // 在实例上调用 getCookies 方法
            Map<String, String> cookies = eduSystemLoginUtil.getCookies(eduUsername);
            System.out.println("获取了默认学期");
            HashMap<String, String> years = new HashMap<>();
            HashMap<String, String> semesters = new HashMap<>();
            String defaultYears = null;
            String defaultTeamVal = null;

            Map<String, String> headers = HTTPUtil.createCommonHeaders();

            Connection.Response response = HTTPUtil.sendGetRequest("/jwglxt/cjcx/cjcx_cxDgXscj.html?gnmkdm=N305005&layout=default&su=" + eduUsername, headers, cookies);
            System.out.println("发送了获取学期起止日期的请求");
            Document document = Jsoup.parse(response.body());

            for (Element e : Objects.requireNonNull(document.getElementById("xnm")).getElementsByTag("option")) {

                if ("selected".equals(e.attr("selected"))) {
                    defaultYears = e.text();
                }
                years.put(e.text(), e.attr("value"));
            }

            for (Element e : Objects.requireNonNull(document.getElementById("xqm")).getElementsByTag("option")) {
                if (!e.attr("selected").isEmpty()) {
                    defaultTeamVal = e.text();
                }
                semesters.put(e.text(), e.attr("value"));
            }
            Term term = new Term(defaultYears, defaultTeamVal);
            return new YearAndSemestersPicker(years, semesters, term);
        } catch (SocketTimeoutException e) {
            throw new LoginException("服务器响应时间过长，请稍后再试！！！");
        } catch (NullPointerException e) {
            throw new LoginException("获取学期失败，请检查教务系统账号密码是否进行绑定");
        } catch (Exception e) {
            logger.error("获取学期失败", e);
            throw new LoginException(e.getMessage());
        }
    }

}
