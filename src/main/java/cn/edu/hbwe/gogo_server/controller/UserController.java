package cn.edu.hbwe.gogo_server.controller;

import cn.edu.hbwe.gogo_server.dto.Result;
import cn.edu.hbwe.gogo_server.entity.User;
import cn.edu.hbwe.gogo_server.entity.WeChatLogin;
import cn.edu.hbwe.gogo_server.service.UserService;
import cn.edu.hbwe.gogo_server.utils.JWTUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * @author Photite
 */
@RestController
@RequestMapping("/user")
public class UserController {

    // 引入日志记录器
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 注入 StringRedisTemplate
    @Autowired
    private StringRedisTemplate redisTemplate;

    // 注入 UserService
    @Autowired
    private UserService userService;

    // 定义一个获取用户信息的请求
//    @PostMapping("/login")
//    public Result login(@RequestBody User object) {
//        // 调用UserService的方法获取用户信息
//        User user = userService.login(object.getUsername(), object.getPassword());
//        Result vo;
//        if (user != null) {
//            //登录成功
//            /*
//              1.生成一个token  字符串  比较长，随机
//              */
////            String token = tokenUtils.createToken(object.getId() + "", object.getUsername());
//            //将token保存到redis
////            userService.saveToken(token);
//            vo = new Result("登录成功", "1000", user);
//        } else {
//            vo = new Result("账号或者密码错误", "2000", null);
//        }
//        return vo;
//    }

    @PostMapping("/login")
    public ResponseEntity<Result> loginCheck(@RequestBody WeChatLogin weChatLogin, HttpServletResponse response) throws Exception {
        // 检查登录
        System.out.println(weChatLogin.getCode());
        Map<String, Object> resultMap = userService.Login(weChatLogin.getCode(), weChatLogin.getEduUsername(), weChatLogin.getEduPassword());
        // resultMap大于1为通过，业务层判断正确后返回用户信息和token，所以应该size为2才正确。
        if (resultMap.size() > 1) {
            logger.info("创建的token为=>{}", resultMap.get("token"));
            // 将token添加入响应头以及返回用户信息
            response.setHeader(JWTUtils.header, (String) resultMap.get("token"));
            return new ResponseEntity<Result>(new Result("登录成功", "1000", resultMap.get("user").toString()), HttpStatus.OK);
        } else {
            // 当返回map的size为1时，即为报错信息
            return new ResponseEntity<Result>(new Result("登录失败", "2000", resultMap.get("errmsg").toString()), HttpStatus.OK);
        }
    }

    //检查用户名是否存在
    @GetMapping("/checkUserName")
    public Result checkUserName(@RequestParam String name) {
        // 根据用户名检查是否存在，这里假设 username 不存在
        User user = userService.findByUsername(name);
        System.out.println(user);
        Result vo;
        if (user != null) {
            //重复了
            vo = new Result("用户名重复了", "1000", null);
        } else {
            //可以注册
            vo = new Result("用户名不重复", "2000", null);
        }
        return vo;
    }

    //用户注册
    @PostMapping("/register")
    public Result Register(@RequestBody User object) {
        boolean flag = userService.register(object.getUsername(), object.getPassword());
        Result vo;
        if (flag) {
            //注册成功
            vo = new Result("注册成功", "1000", null);
        } else {
            //注册成功
            vo = new Result("用户信息注册失败", "2000", null);
        }
        return vo;
    }

    // 生成一个测试方法
//    @GetMapping("/test")
//    public String test() throws JsonProcessingException {
//        userService.testSendSubscribeMessage();
//        return "Hello World!";
//    }

}
