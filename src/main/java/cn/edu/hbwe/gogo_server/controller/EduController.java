package cn.edu.hbwe.gogo_server.controller;

import cn.edu.hbwe.gogo_server.dto.Result;
import cn.edu.hbwe.gogo_server.service.EduService;
import cn.edu.hbwe.gogo_server.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/getTimetable")
    public ResponseEntity<Result> getTimetable(@RequestParam String eduUsername) throws Exception {
        logger.info("开始获取课表，用户名：{}", eduUsername);
        // 调用 UserService 的 getClassTable 方法，返回课表内容
        Result result = eduService.getClassTable(eduUsername);
        logger.info("获取课表成功，用户名：{}", eduUsername);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
