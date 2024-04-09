package cn.edu.hbwe.gogo_server.controller;

import cn.edu.hbwe.gogo_server.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Photite
 */
@RestController
@RequestMapping("/user")
public class UserController {

    // 引入日志记录器
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 注入 UserService
    @Autowired
    private UserService userService;

//    @GetMapping("/getTimetable")
//    public ResponseEntity<Result> getTimetable(@RequestParam String eduUsername) {
//        try {
//            // 调用 UserService 的 getClassTable 方法，返回课表内容
//            Result result = userService.getClassTable(eduUsername);
//            return new ResponseEntity<>(result, HttpStatus.OK);
//        } catch (Exception e) {
//            logger.error("获取课表失败", e);
//            return new ResponseEntity<>(new Result("获取课表失败", "2002", null), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }


}
