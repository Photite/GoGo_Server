package cn.edu.hbwe.gogo_server.service;

import cn.edu.hbwe.gogo_server.dao.ClassUnitDao;
import cn.edu.hbwe.gogo_server.dto.Result;
import cn.edu.hbwe.gogo_server.entity.ClassUnit;
import cn.edu.hbwe.gogo_server.entity.Profile;
import cn.edu.hbwe.gogo_server.entity.Term;
import cn.edu.hbwe.gogo_server.entity.YearAndSemestersPicker;
import cn.edu.hbwe.gogo_server.exception.LoginException;
import cn.edu.hbwe.gogo_server.utils.EduSystemLoginUtil;
import cn.edu.hbwe.gogo_server.utils.HTTPUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Photite
 */
@Service
public class ClassTableService {
    // 引入Log4j2日志 日志记录器
    private static final Logger logger = LogManager.getLogger(EduSystemLoginUtil.class);

    //创建EduSystemLoginUtil实例
    @Autowired
    private EduSystemLoginUtil eduSystemLoginUtil;

    // 创建EduService实例
    @Autowired
    private EduService eduService;

    @Autowired
    private ClassUnitDao classUnitDao;

//    // 定义一个获取课表的方法，接收学号，返回Result
//    public Result getClassTable(String eduUsername) {
//        try {
//            // 在实例上调用 getCookies 方法
//            Map<String, String> cookies = eduSystemLoginUtil.getCookies(eduUsername);
//            System.out.println("cookies" + cookies);
//            YearAndSemestersPicker picker = eduService.getPicker(eduUsername);
//            Map<String, String> headers = HTTPUtil.createCommonHeaders();
//            Term term = picker.getDefaultTerm();
//
//            String xnm = picker.getYears().get(term.getYearsOfSchooling());
//            String xqm = picker.getSemesters().get(term.getSemesterNumber());
//            System.out.println("学年：" + xnm);
//            System.out.println("学期：" + xqm);
//
//            Map<String, String> data = new HashMap<>();
//            data.put("xnm", String.valueOf(picker.getYears().get(term.getYearsOfSchooling())));
//            data.put("xqm", String.valueOf(picker.getSemesters().get(term.getSemesterNumber())));
//
//
//            Connection.Response response = HTTPUtil.sendPostRequest("/jwglxt/kbcx/xskbcx_cxXsKb.html?gnmkdm=N2151", headers, data, cookies);
//            System.out.println("发送了请求");
//
//            String body = response.body();
//
//            com.alibaba.fastjson2.JSONArray array = com.alibaba.fastjson2.JSON.parseObject(body).getJSONArray("kbList");
//            if (array.isEmpty()) {
//                throw new IllegalStateException("该学年学期的课表尚未开放!");
//            }
//            List<ClassUnit> timetable = array.stream()
//                    .map((v) -> {
//                        com.alibaba.fastjson2.JSONObject a = (com.alibaba.fastjson2.JSONObject) v;
//                        String lesson = a.getString("jcs");
//                        String[] ls = lesson.split("-");
//
//                        return new ClassUnit(
//                                a.getString("kcmc"),
//                                a.getString("xm"),
//                                a.getString("cdmc"),
//                                a.getString("zcd"),
//                                new ClassUnit.Range(Integer.parseInt(ls[0]), Integer.parseInt(ls[1]), ClassUnit.FilterType.ALL),
//                                a.getString("kch"),
//                                a.getString("xqj")
//                        );
//                    })
//                    .toList();
//            System.out.println(timetable);
//            return new Result("获取课表成功", "1000", timetable);
//        } catch (SocketTimeoutException e) {
//            throw new LoginException("服务器响应时间过长，请稍后再试！！！");
//        } catch (NullPointerException e) {
//            throw new LoginException("获取课表失败，请检查教务系统账号密码是否进行绑定");
//        } catch (Exception e) {
//            logger.error("获取课表失败", e);
//            throw new LoginException(e.getMessage());
//        }
//    }


    // 定义一个获取课表的方法，接收学号，返回Result
    public Result getClassTable(String eduUsername) {
        try {
            List<ClassUnit> timetable = getClassUnit(eduUsername);
            return new Result("获取课表成功", "1000", timetable);
        } catch (NullPointerException e) {
            throw new LoginException("获取课表失败，请检查教务系统账号密码是否进行绑定");
        } catch (Exception e) {
            logger.error("获取课表失败", e);
            throw new LoginException(e.getMessage());
        }
    }


    private List<ClassUnit> getClassUnit (String eduUsername) {
        QueryWrapper<ClassUnit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("edu_username", eduUsername);
//        List<ClassUnit> classUnit = classUnitDao.selectList(queryWrapper);
        List<ClassUnit> classUnitList = new ArrayList<>();
        List<Map<String, Object>> records = classUnitDao.selectMaps(queryWrapper);
        for (Map<String, Object> record : records) {
            ClassUnit classUnit = new ClassUnit(
                    (int) record.get("lessonStart"),
                    (String) record.get("name"),
                    (String) record.get("teacher"),
                    (String) record.get("room"),
                    (int) record.get("lessonEnd"),
                    (String) record.get("lessonType"),
                    (String) record.get("code"),
                    (int) record.get("dayInWeek"),
                    (int) record.get("weekStart"),
                    (int) record.get("weekEnd"),
                    (String) record.get("weekType"),
                    (String) record.get("weekEachLesson"),
                    (String) record.get("eduUsername"),
                    (String) record.get("openid"),
                    (String) record.get("classInfo")
            );
            classUnitList.add(classUnit);
        }
        System.out.println("从数据库中获取的课程表信息: " + classUnitList);
        if (classUnitList.isEmpty()) {
            classUnitList = getClassUnitFromJsoup(eduUsername);
            System.out.println("从爬虫中获取的课程表信息: " + classUnitList);
            // 将爬取的数据保存到数据库中
            for (ClassUnit unit : classUnitList) {
                classUnitDao.insert(unit);
            }
        }
        return classUnitList;
    }

    private List<ClassUnit> getClassUnitFromJsoup(String eduUsername) {
        try {
            // 在实例上调用 getCookies 方法
            Map<String, String> cookies = eduSystemLoginUtil.getCookies(eduUsername);
            System.out.println("cookies" + cookies);
            YearAndSemestersPicker picker = eduService.getPicker(eduUsername);
            Map<String, String> headers = HTTPUtil.createCommonHeaders();
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


                        ClassUnit unit = new ClassUnit(
                                a.getString("kcmc"),
                                a.getString("xm"),
                                a.getString("cdmc"),
                                a.getString("zcd"),//  new ClassUnit.Range(Integer.parseInt(ls[0]), Integer.parseInt(ls[1]), ClassUnit.FilterType.ALL),
                                Integer.parseInt(ls[0]), // lessonStart
                                Integer.parseInt(ls[1]), // lessonEnd
                                ClassUnit.FilterType.ALL.toString(), // lessonType
                                a.getString("kch"),
                                a.getString("xqj")
                        );
                        // 设置eduUsername字段
                        unit.setEduUsername(eduUsername);
                        return unit;
                    })
                    .toList();
            return timetable;
        } catch (SocketTimeoutException e) {
            throw new LoginException("服务器响应时间过长，请稍后再试！！！");
        } catch (Exception e) {
            logger.error("获取课程表信息失败", e);
            throw new LoginException(e.getMessage());
        }
    }


}
