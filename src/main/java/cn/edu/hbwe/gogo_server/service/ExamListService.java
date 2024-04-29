package cn.edu.hbwe.gogo_server.service;

import cn.edu.hbwe.gogo_server.dto.Result;
import cn.edu.hbwe.gogo_server.entity.ExamResult;
import cn.edu.hbwe.gogo_server.entity.Term;
import cn.edu.hbwe.gogo_server.entity.YearAndSemestersPicker;
import cn.edu.hbwe.gogo_server.exception.LoginException;
import cn.edu.hbwe.gogo_server.utils.EduSystemLoginUtil;
import cn.edu.hbwe.gogo_server.utils.HTTPUtil;
import com.alibaba.fastjson2.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;
import java.util.*;

/**
 * @author Photite
 */
@Service
public class ExamListService {
    // 引入Log4j2日志 日志记录器
    private static final Logger logger = LogManager.getLogger(EduSystemLoginUtil.class);

    //创建EduSystemLoginUtil实例
    @Autowired
    private EduSystemLoginUtil eduSystemLoginUtil;

    // 创建EduService实例
    @Autowired
    private EduService eduService;

    // 定义一个获取考试成绩的方法，接收学号，返回Result
    public Result getExamList(String eduUsername) {
        try {
            Map<String, String> cookies = eduSystemLoginUtil.getCookies(eduUsername);
            YearAndSemestersPicker picker = eduService.getPicker(eduUsername);
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
            Map<String, String> headers = HTTPUtil.createCommonHeaders();

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

            List<JSONObject> jsonObjects = com.alibaba.fastjson2.JSON.parseArray(items, com.alibaba.fastjson2.JSONObject.class);

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
            Map<String, String> headers = HTTPUtil.createCommonHeaders();

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


}
