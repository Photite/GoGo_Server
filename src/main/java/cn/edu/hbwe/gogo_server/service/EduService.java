package cn.edu.hbwe.gogo_server.service;

import cn.edu.hbwe.gogo_server.dao.UserDao;
import cn.edu.hbwe.gogo_server.dto.Result;
import cn.edu.hbwe.gogo_server.entity.ClassUnit;
import cn.edu.hbwe.gogo_server.entity.Term;
import cn.edu.hbwe.gogo_server.entity.YearAndSemestersPicker;
import cn.edu.hbwe.gogo_server.exception.LoginException;
import cn.edu.hbwe.gogo_server.utils.EduSystemLoginUtil;
import cn.edu.hbwe.gogo_server.utils.HTTPUtil;
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
public class EduService {

    // 引入Log4j2日志 日志记录器
    private static final Logger logger = LogManager.getLogger(EduSystemLoginUtil.class);

    // 引入StringRedisTemplate类实例
    @Autowired
    private StringRedisTemplate redisTemplate;

    //创建EduSystemLoginUtil实例
    @Autowired
    private EduSystemLoginUtil eduSystemLoginUtil;

    public Result getClassTable(String eduUsername) {
        try {
            // 在实例上调用 getCookies 方法
            Map<String, String> cookies = eduSystemLoginUtil.getCookies(eduUsername);
            System.out.println("cookies" + cookies);
            YearAndSemestersPicker picker = getPicker(eduUsername);
            Map<String, String> headers = createCommonHeaders();
            Term term = picker.getDefaultTerm();

            String xnm = picker.getYears().get(term.getYearsOfSchooling());
            String xqm = picker.getSemesters().get(term.getSemesterNumber());
            System.out.println("学年：" + xnm);
            System.out.println("学期：" + xqm);

            Map<String, String> data = new HashMap<>();
            data.put("xnm", String.valueOf(picker.getYears().get(term.getYearsOfSchooling())));
            data.put("xqm", String.valueOf(picker.getSemesters().get(term.getSemesterNumber())));


            Connection.Response response = HTTPUtil.sendPostRequest("/jwglxt/kbcx/xskbcx_cxXsKb.html?gnmkdm=N2151", headers, data, cookies);
            System.out.println("发送了请求");

            String body = response.body();

            com.alibaba.fastjson2.JSONArray array = com.alibaba.fastjson2.JSON.parseObject(body).getJSONArray("kbList");
            if (array.isEmpty()) {
                throw new IllegalStateException("该学年学期的课表尚未开放!");
            }
            List<ClassUnit> timetable = array.stream()
                    .map((v) -> {
                        com.alibaba.fastjson2.JSONObject a = (com.alibaba.fastjson2.JSONObject) v;
                        String lesson = a.getString("jcs");
                        String[] ls = lesson.split("-");

                        return new ClassUnit(
                                a.getString("kcmc"),
                                a.getString("xm"),
                                a.getString("cdmc"),
                                a.getString("zcd"),
                                new ClassUnit.Range(Integer.parseInt(ls[0]), Integer.parseInt(ls[1]), ClassUnit.FilterType.ALL),
                                a.getString("kch"),
                                a.getString("xqj")
                        );
                    })
                    .toList();
            System.out.println(timetable);
            return new Result("获取课表成功", "1000", timetable);
        } catch (SocketTimeoutException e) {
            throw new LoginException("服务器响应时间过长，请稍后再试！！！");
        } catch (NullPointerException e) {
            throw new LoginException("获取课表失败，请检查教务系统账号密码是否进行绑定");
        } catch (Exception e) {
            logger.error("获取课表失败", e);
            throw new LoginException(e.getMessage());
        }
    }

    public YearAndSemestersPicker getPicker(String eduUsername) {
        try {
            // 在实例上调用 getCookies 方法
            Map<String, String> cookies = eduSystemLoginUtil.getCookies(eduUsername);
            System.out.println("获取了默认学期");
            HashMap<String, String> years = new HashMap<>();
            HashMap<String, String> semesters = new HashMap<>();
            String defaultYears = null;
            String defaultTeamVal = null;

            Map<String, String> headers = createCommonHeaders();

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

//    private void getCookiesWithRetry(String eduUsername) {
//        try {
//            // 如果cookie过期，重新登录并获取新的cookie
//            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//            queryWrapper.eq("edu_username", eduUsername);
//            User user = userDao.selectOne(queryWrapper);
//            Map<String, String> newCookies = eduSystemLoginUtil.loginAndGetCookies(eduUsername, user.getEduPassword());
//            // 将新的cookie存储到Redis中
//            ObjectMapper mapper = new ObjectMapper();
//            String newCookieJson = mapper.writeValueAsString(newCookies);
//            redisTemplate.boundValueOps(eduUsername).set(newCookieJson);
//            redisTemplate.expire(eduUsername, 7, TimeUnit.HOURS);
//        } catch (Exception ex) {
//            logger.error("重新获取Cookie失败", ex);
//        }
//    }

    private Map<String, String> createCommonHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        return headers;
    }

}
