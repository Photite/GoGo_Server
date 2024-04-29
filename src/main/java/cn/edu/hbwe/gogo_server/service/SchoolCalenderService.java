package cn.edu.hbwe.gogo_server.service;

import cn.edu.hbwe.gogo_server.dto.Result;
import cn.edu.hbwe.gogo_server.entity.SchoolCalender;
import cn.edu.hbwe.gogo_server.entity.Term;
import cn.edu.hbwe.gogo_server.exception.LoginException;
import cn.edu.hbwe.gogo_server.utils.EduSystemLoginUtil;
import cn.edu.hbwe.gogo_server.utils.HTTPUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.util.Map;

/**
 * @author Photite
 */
@Service
public class SchoolCalenderService {
    // 引入Log4j2日志 日志记录器
    private static final Logger logger = LogManager.getLogger(EduSystemLoginUtil.class);

    //创建EduSystemLoginUtil实例
    @Autowired
    private EduSystemLoginUtil eduSystemLoginUtil;

    // 定义一个查询学校日期的方法（包括：学期起止时间，当前学期数），接收学号和cookie，返回SchoolCalender
    public Result getSchoolCalender(String eduUsername) {
        try {
            Map<String, String> cookies = eduSystemLoginUtil.getCookies(eduUsername);
            Map<String, String> headers = HTTPUtil.createCommonHeaders();

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

}
