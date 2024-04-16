package cn.edu.hbwe.gogo_server.service;

import cn.edu.hbwe.gogo_server.dto.Info;
import cn.edu.hbwe.gogo_server.dto.Result;
import cn.edu.hbwe.gogo_server.entity.*;
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
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;
import java.time.LocalDate;
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

    // 定义一个获取课表的方法，接收学号，返回Result
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

    // 定义一个获取用户信息的方法，接收学号，返回Result
    public Result getUserProfile(String eduUsername) {
        try {
            Map<String, String> cookies = eduSystemLoginUtil.getCookies(eduUsername);

            Map<String, String> headers = createCommonHeaders();

            Connection.Response response = HTTPUtil.sendGetRequest("/jwglxt/xsxxxggl/xsgrxxwh_cxXsgrxx.html?gnmkdm=N100801&layout=default&su=" + eduUsername, headers, cookies);

            Document document = Jsoup.parse(response.body());

            Elements ele = document.getElementsByClass("form-control-static");

            Profile profile = new Profile(
                    ele.get(0).text(),
                    ele.get(1).text(),
                    ele.get(23).text(),
                    ele.get(24).text(),
                    ele.get(26).text(),
                    ele.get(9).text());
            List<Info> infoList = new ArrayList<>();
            infoList.add(new Info("学号", profile.getNo()));
            infoList.add(new Info("姓名", profile.getName()));
            infoList.add(new Info("年级", profile.getGrade()));
            infoList.add(new Info("学院", profile.getCollegeName()));
            infoList.add(new Info("专业", profile.getStudyName()));
            infoList.add(new Info("身份证", profile.getIdCard()));
            String gpa = getGPAScores(eduUsername);
            infoList.add(new Info("绩点", gpa));
            Map<String, Object> data = new HashMap<>();
            data.put("info", infoList);
            return new Result("获取用户信息成功", "1000", data);
        } catch (SocketTimeoutException e) {
            throw new LoginException("服务器响应时间过长，请稍后再试！！！");
        } catch (NullPointerException e) {
            throw new LoginException("获取个人信息失败，请检查教务系统账号密码是否进行绑定");
        } catch (Exception e) {
            logger.error("获取个人信息失败", e);
            throw new LoginException(e.getMessage());
        }
    }

    // 定义一个获取GPA的方法，接收学号，返回String
    public String getGPAScores(String eduUsername) {
        try {
            Map<String, String> cookies = eduSystemLoginUtil.getCookies(eduUsername);
            Map<String, String> headers = createCommonHeaders();
            String gpa = "";
            Connection.Response response = HTTPUtil.sendGetRequest("/jwglxt/xsxy/xsxyqk_cxXsxyqkIndex.html?gnmkdm=N105515&layout=default&su=" + eduUsername, headers, cookies);
            String body = response.body();

            Document doc = Jsoup.parse(body);
            // 使用选择器定位所有红色的<font>元素
            Elements elements = doc.select("font[style='color: red;']");

            for (Element element : elements) {
                String text = element.text().trim();
                try {
                    // 尝试将文本转换为浮点数
                    Float.parseFloat(text);
                    // 如果没有抛出异常，那么这个元素的文本就是GPA
                    System.out.println("GPA: " + text);
                    gpa = text;
                    break;  // 找到GPA后就可以停止遍历
                } catch (NumberFormatException e) {
                    // 如果抛出异常，那么这个元素的文本不是GPA，继续遍历下一个元素
                }
            }
            return gpa;
        } catch (SocketTimeoutException e) {
            throw new LoginException("服务器响应时间过长，请稍后再试！！！");
        } catch (NullPointerException e) {
            throw new LoginException("获取GPA失败，请检查教务系统账号密码是否进行绑定");
        } catch (Exception e) {
            logger.error("获取GPA失败", e);
            throw new LoginException(e.getMessage());
        }
    }

    // 定义一个创建公共请求头的方法，返回一个Map<String, String>对象
    private Map<String, String> createCommonHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        return headers;
    }

    // 定义一个查询学校日期的方法（包括：学期起止时间，当前学期数），接收学号和cookie，返回SchoolCalender
    public Result getSchoolCalender(String eduUsername) {
        try {
            Map<String, String> cookies = eduSystemLoginUtil.getCookies(eduUsername);
            Map<String, String> headers = createCommonHeaders();

            Connection.Response response = HTTPUtil.sendGetRequest("/jwglxt/xtgl/index_cxAreaSix.html?localeKey=zh_CN&gnmkdm=index&su=" + eduUsername, headers, cookies);

            Document document = Jsoup.parse(response.body());

            String source = document.getElementsByAttributeValue("colspan", "23").get(0).text();

            String year = source.split("学年")[0];
            String sem = source.split("学年")[1].split("学期")[0];

            int l, r;
            l = source.indexOf("(");
            r = source.indexOf(")");
            source = source.substring(l + 1, r);
            String[] se = source.split("至");

            String[] starts = se[0].split("-");
            LocalDate start = LocalDate.of(Integer.parseInt(starts[0]), Integer.parseInt(starts[1]), Integer.parseInt(starts[2]));
            starts = se[1].split("-");
            LocalDate end = LocalDate.of(Integer.parseInt(starts[0]), Integer.parseInt(starts[1]), Integer.parseInt(starts[2]));

            Term term1 = new Term(year, sem);
            return new Result("获取学期起止时间成功", "1000", new SchoolCalender(start, end, term1));

        } catch (SocketTimeoutException e) {
            throw new LoginException("服务器响应时间过长，请稍后再试！！！");
        } catch (NullPointerException e) {
            throw new LoginException("获取学期起止时间失败，请检查教务系统账号密码是否进行绑定");
        } catch (Exception e) {
            logger.error("获取学期起止时间失败", e);
            throw new LoginException(e.getMessage());
        }
    }

    // 定义一个获取考试成绩的方法，接收学号，返回Result
    public Result getExamList(String eduUsername) {
        try {
            Map<String, String> cookies = eduSystemLoginUtil.getCookies(eduUsername);
            YearAndSemestersPicker picker = getPicker(eduUsername);
            Term term = picker.getDefaultTerm();
            String xnm = picker.getYears().get(term.getYearsOfSchooling());
            String xqm = picker.getSemesters().get(term.getSemesterNumber());
            System.out.println("学期：" + xqm);
            try {
                Objects.requireNonNull(xnm);
                Objects.requireNonNull(xqm);
            } catch (NullPointerException e) {
                throw new IllegalStateException("学期值非法!");
            }
            Map<String, String> headers = createCommonHeaders();

            Map<String, String> data = new HashMap<>();
            data.put("xnm", xnm);
            data.put("xqm", xqm);
            data.put("kcbj", "");
            data.put("_search", "false");
            data.put("nd", String.valueOf(new Date().getTime()));
            data.put("queryModel.showCount", "15");
            data.put("queryModel.currentPage", "1");
            data.put("queryModel.sortName", "");
            data.put("queryModel.sortOrder", "asc");
            data.put("time", "2");

            Connection.Response response = HTTPUtil.sendPostRequest("/jwglxt/cjcx/cjcx_cxXsgrcj.html?doType=query&gnmkdm=N305005&su=" + eduUsername, headers, data, cookies);

            String body = response.body();


            String items = com.alibaba.fastjson2.JSON.parseObject(body).getJSONArray("items").toString();

            List<com.alibaba.fastjson2.JSONObject> jsonObjects = com.alibaba.fastjson2.JSON.parseArray(items, com.alibaba.fastjson2.JSONObject.class);

            // 遍历JSONObject列表
            for (com.alibaba.fastjson2.JSONObject jsonObject : jsonObjects) {
                // 获取sfxwkc字段的值
                String sfxwkc = jsonObject.getString("sfxwkc");
                // 将"是"转换为true，将"否"转换为false
                jsonObject.put("sfxwkc", "是".equals(sfxwkc));
            }
            List<ExamResult> exam = jsonObjects.stream()
                    .map(jsonObject -> com.alibaba.fastjson2.JSON.toJavaObject(jsonObject, ExamResult.class))
                    .toList();
            List<Map<String, Object>> simplifiedExamResults = new ArrayList<>();
            for (ExamResult examResult : exam) {
                Map<String, Object> simplifiedExamResult = new HashMap<>();
                simplifiedExamResult.put("name", examResult.getName());
                simplifiedExamResult.put("absoluteScore", examResult.getAbsoluteScore());
                simplifiedExamResults.add(simplifiedExamResult);
            }
            return new Result("获取考试成绩成功", "1000", simplifiedExamResults);

        } catch (SocketTimeoutException e) {
            throw new LoginException("服务器响应时间过长，请稍后再试！！！");
        } catch (NullPointerException e) {
            throw new LoginException("获取考试成绩失败，请检查教务系统账号密码是否进行绑定");
        } catch (Exception e) {
            logger.error("获取考试成绩失败", e);
            throw new LoginException(e.getMessage());
        }
    }

    // 定义一个获取全部考试成绩的方法，接收学号，返回Result
    public Result getAllExamList(String eduUsername) {
        try {
            Map<String, String> cookies = eduSystemLoginUtil.getCookies(eduUsername);
            Map<String, String> headers = createCommonHeaders();

            Map<String, String> data = new HashMap<>();
            data.put("xnm", "");
            data.put("xqm", "");
            data.put("kcbj", "");
            data.put("_search", "false");
            data.put("nd", String.valueOf(new Date().getTime()));
            data.put("queryModel.showCount", "90");
            data.put("queryModel.currentPage", "1");
            data.put("queryModel.sortName", "");
            data.put("queryModel.sortOrder", "asc");
            data.put("time", "2");

            Connection.Response response = HTTPUtil.sendPostRequest("/jwglxt/cjcx/cjcx_cxXsgrcj.html?doType=query&gnmkdm=N305005&su=" + eduUsername, headers, data, cookies);

            String body = response.body();


            String items = com.alibaba.fastjson2.JSON.parseObject(body).getJSONArray("items").toString();

            List<com.alibaba.fastjson2.JSONObject> jsonObjects = com.alibaba.fastjson2.JSON.parseArray(items, com.alibaba.fastjson2.JSONObject.class);

            // 遍历JSONObject列表
            for (com.alibaba.fastjson2.JSONObject jsonObject : jsonObjects) {
                // 获取sfxwkc字段的值
                String sfxwkc = jsonObject.getString("sfxwkc");
                // 将"是"转换为true，将"否"转换为false
                jsonObject.put("sfxwkc", "是".equals(sfxwkc));
            }

            List<ExamResult> exam = jsonObjects.stream()
                    .map(jsonObject -> com.alibaba.fastjson2.JSON.toJavaObject(jsonObject, ExamResult.class))
                    .toList();
            Map<String, List<Map<String, Object>>> examResultsBySemester = new LinkedHashMap<>();
            for (ExamResult examResult : exam) {
                Map<String, Object> simplifiedExamResult = new HashMap<>();
                simplifiedExamResult.put("name", examResult.getName());
                simplifiedExamResult.put("absoluteScore", examResult.getAbsoluteScore());
                String semesterKey = examResult.getYear() + "-" + examResult.getSemester();
                if (!examResultsBySemester.containsKey(semesterKey)) {
                    examResultsBySemester.put(semesterKey, new ArrayList<>());
                }
                examResultsBySemester.get(semesterKey).add(simplifiedExamResult);
            }
            return new Result("获取全部考试成绩成功", "1000", new ArrayList<>(examResultsBySemester.values()));
        } catch (SocketTimeoutException e) {
            throw new LoginException("服务器响应时间过长，请稍后再试！！！");
        } catch (NullPointerException e) {
            throw new LoginException("获取全部考试成绩失败，请检查教务系统账号密码是否进行绑定");
        } catch (Exception e) {
            logger.error("获取全部考试成绩失败", e);
            throw new LoginException(e.getMessage());
        }
    }

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
            redisTemplate.expire(eduUsername, 7, TimeUnit.HOURS);
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

}
