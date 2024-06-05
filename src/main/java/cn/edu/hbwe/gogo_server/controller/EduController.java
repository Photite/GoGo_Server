package cn.edu.hbwe.gogo_server.controller;

import cn.edu.hbwe.gogo_server.dto.Result;
import cn.edu.hbwe.gogo_server.entity.ExamResult;
import cn.edu.hbwe.gogo_server.entity.User;
import cn.edu.hbwe.gogo_server.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * @author Photite
 */
@RestController
@RequestMapping("/edu")
public class EduController {

    // 注入 EduService
    @Autowired
    private EduService eduService;

    // 注入 UserProfileService
    @Autowired
    private UserProfileService userProfileService;

    // 注入 ClassTableService
    @Autowired
    private ClassTableService classTableService;

    // 注入 ExamListService
    @Autowired
    private ExamResultListService examResultListService;

    // 注入 SchoolCalenderService
    @Autowired
    private SchoolCalenderService schoolCalenderService;

    // 引入日志记录器
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 定义一个模拟登录教务系统的请求，接收学号和密码
    @PostMapping("/stuLogin")
    public ResponseEntity<Result> stulogin(@RequestBody User o) {
        return new ResponseEntity<>(eduService.eduLogin(o.getEduUsername(), o.getEduPassword()), HttpStatus.OK);
    }

    // 定义一个获取课表的请求
    @GetMapping("/getTimetable")
    public ResponseEntity<Result> getTimetable(@RequestParam String eduUsername) {
        logger.info("开始获取课表，用户名：{}", eduUsername);
        // 调用 UserService 的 getClassTable 方法，返回课表内容
        Result result = classTableService.getClassTable(eduUsername);
        logger.info("获取课表成功，用户名：{}", eduUsername);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 定义一个获取用户信息的请求
    @GetMapping("/getUserProfile")
    public ResponseEntity<Result> getUserProfile(@RequestParam String eduUsername) {
        // 调用 UserService 的 getProfile 方法，返回一个字符串表示用户信息
        return new ResponseEntity<>(userProfileService.getUserProfile(eduUsername), HttpStatus.OK);
    }

    // 定义一个查询学校当前学期起止时间的请求
    @GetMapping("/getSchoolCalender")
    public ResponseEntity<Result> getSchoolCalender(@RequestParam String eduUsername) {
        return new ResponseEntity<>(schoolCalenderService.getSchoolCalender(eduUsername), HttpStatus.OK);
    }

    // 定义一个获取考试分数的请求
    @GetMapping("/getExamGrade")
    public ResponseEntity<Result> getExamGrade(@RequestParam String eduUsername) {
        return new ResponseEntity<>(examResultListService.getExamResultList(eduUsername), HttpStatus.OK);
    }

//    // 定义一个获取所有考试分数的请求
//    @GetMapping("/getAllExamGrade")
//    public ResponseEntity<Result> getAllExamGrade(@RequestParam String eduUsername) {
//        return new ResponseEntity<>(examListService.getAllExamList(eduUsername), HttpStatus.OK);
//    }
}
