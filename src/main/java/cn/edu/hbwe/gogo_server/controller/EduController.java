package cn.edu.hbwe.gogo_server.controller;

import cn.edu.hbwe.gogo_server.dto.Info;
import cn.edu.hbwe.gogo_server.dto.Result;
import cn.edu.hbwe.gogo_server.entity.Profile;
import cn.edu.hbwe.gogo_server.service.EduService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Photite
 */
@RestController
@RequestMapping("/edu")
public class EduController {

    // 注入 EduService
    @Autowired
    private EduService eduService;

    // 引入日志记录器
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 定义一个获取课表的请求
    @GetMapping("/getTimetable")
    public ResponseEntity<Result> getTimetable(@RequestParam String eduUsername) {
        logger.info("开始获取课表，用户名：{}", eduUsername);
        // 调用 UserService 的 getClassTable 方法，返回课表内容
        Result result = eduService.getClassTable(eduUsername);
        logger.info("获取课表成功，用户名：{}", eduUsername);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 定义一个获取用户信息的请求
    @GetMapping("/getUserProfile")
    public Result getUserProfile(@RequestParam String eduUsername) {
        try {
            // 调用 UserService 的 getProfile 方法，返回一个字符串表示用户信息
            Profile profile = eduService.getUserProfile(eduUsername);
            // 创建一个列表来存储Info对象
            List<Info> infoList = new ArrayList<>();
            // 将Profile对象的每个字段转换为一个Info对象，并添加到列表中
            infoList.add(new Info("学号", profile.getNo()));
            infoList.add(new Info("姓名", profile.getName()));
            infoList.add(new Info("年级", profile.getGrade()));
            infoList.add(new Info("学院", profile.getCollegeName()));
            infoList.add(new Info("专业", profile.getStudyName()));
            infoList.add(new Info("身份证", profile.getIdCard()));
            String gpa = eduService.getGPAScores(eduUsername);
            infoList.add(new Info("绩点", gpa));
            Map<String, Object> data = new HashMap<>();
            data.put("info", infoList);
            // 返回 200 状态码和用户信息
            System.out.println("获取用户信息成功");
            return new Result("获取用户信息成功", "1000", data);
        } catch (Exception e) {
            // 如果发生异常，返回 500 状态码和异常信息
            System.out.println("获取用户信息失败");
            return new Result("获取用户信息失败", "2002", null);
        }
    }
}
