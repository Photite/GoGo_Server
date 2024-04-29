package cn.edu.hbwe.gogo_server.service;

import cn.edu.hbwe.gogo_server.dao.ProfileDao;
import cn.edu.hbwe.gogo_server.dto.Info;
import cn.edu.hbwe.gogo_server.dto.Result;
import cn.edu.hbwe.gogo_server.entity.Profile;
import cn.edu.hbwe.gogo_server.exception.LoginException;
import cn.edu.hbwe.gogo_server.utils.EduSystemLoginUtil;
import cn.edu.hbwe.gogo_server.utils.HTTPUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
public class UserProfileService {
    // 引入Log4j2日志 日志记录器
    private static final Logger logger = LogManager.getLogger(EduSystemLoginUtil.class);

    //创建EduSystemLoginUtil实例
    @Autowired
    private EduSystemLoginUtil eduSystemLoginUtil;

    @Autowired
    private ProfileDao profileDao;

    // 定义一个获取用户信息的方法，接收学号，返回Result
    public Result getUserProfile(String eduUsername) {
        try {
            Profile profile = getProfile(eduUsername);
            return new Result("获取用户信息成功", "1000", constructData(profile, eduUsername));
        } catch (NullPointerException e) {
            throw new LoginException("获取个人信息失败，请检查教务系统账号密码是否进行绑定");
        } catch (Exception e) {
            logger.error("获取个人信息失败", e);
            throw new LoginException(e.getMessage());
        }
    }

    private Profile getProfile(String eduUsername) {
        QueryWrapper<Profile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("edu_username", eduUsername);
        Profile profile = profileDao.selectOne(queryWrapper);
        System.out.println("从数据库中获取的profile: " + profile);
        if (profile == null) {
            profile = getProfileFromJsoup(eduUsername);
            System.out.println("从爬虫中获取的profile: " + profile);
        }
        return profile;
    }

    private Profile getProfileFromJsoup(String eduUsername) {
        try {
            Map<String, String> cookies = eduSystemLoginUtil.getCookies(eduUsername);
            Map<String, String> headers = HTTPUtil.createCommonHeaders();
            Connection.Response response = HTTPUtil.sendGetRequest("/jwglxt/xsxxxggl/xsgrxxwh_cxXsgrxx.html?gnmkdm=N100801&layout=default&su=" + eduUsername, headers, cookies);
            Document document = Jsoup.parse(response.body());
            Elements ele = document.getElementsByClass("form-control-static");

            return new Profile(
                    ele.get(0).text(),
                    ele.get(1).text(),
                    ele.get(23).text(),
                    ele.get(24).text(),
                    ele.get(26).text(),
                    ele.get(9).text());
        } catch (SocketTimeoutException e) {
            throw new LoginException("服务器响应时间过长，请稍后再试！！！");
        } catch (Exception e) {
            logger.error("获取个人信息失败", e);
            throw new LoginException(e.getMessage());
        }
    }

    private Map<String, Object> constructData(Profile profile, String eduUsername) {
        List<Info> infoList = new ArrayList<>();
        infoList.add(new Info("学号", profile.getEduUsername()));
        infoList.add(new Info("姓名", profile.getName()));
        infoList.add(new Info("年级", profile.getGrade()));
        infoList.add(new Info("学院", profile.getCollegeName()));
        infoList.add(new Info("专业", profile.getStudyName()));
        infoList.add(new Info("身份证", profile.getIdCard()));
        infoList.add(new Info("绩点", getGPAScores(eduUsername)));
        Map<String, Object> data = new HashMap<>();
        data.put("info", infoList);
        profile.setGpa(infoList.get(6).getValue());
        // 先尝试更新用户的信息
        int updated = profileDao.updateById(profile);
        // 如果更新的记录数为0，那么就插入新的记录
        if (updated == 0) {
            profileDao.insert(profile);
        }
        return data;
    }

    // 定义一个获取GPA的方法，接收学号，返回String
    public String getGPAScores(String eduUsername) {
        try {
            Map<String, String> cookies = eduSystemLoginUtil.getCookies(eduUsername);
            Map<String, String> headers = HTTPUtil.createCommonHeaders();
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

}
