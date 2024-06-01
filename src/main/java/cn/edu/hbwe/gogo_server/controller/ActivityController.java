package cn.edu.hbwe.gogo_server.controller;

import cn.edu.hbwe.gogo_server.dto.Result;
import cn.edu.hbwe.gogo_server.entity.Activity;
import cn.edu.hbwe.gogo_server.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Photite
 */
@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    // 定义一个添加活动的请求
    @PostMapping("/add")
    public ResponseEntity<Result> addActivity(@RequestBody Activity activity) {
        return new ResponseEntity<Result>(activityService.addActivity(activity), HttpStatus.OK);
    }

    // 定义一个删除活动的请求
    @PostMapping("/delete")
    public ResponseEntity<Result> deleteActivity(@RequestBody Activity activity) {
        return new ResponseEntity<Result>(activityService.deleteActivity(activity), HttpStatus.OK);
    }

    // 定义一个修改活动的请求
    @PostMapping("/update")
    public ResponseEntity<Result> updateActivity(@RequestBody Activity activity) {
        return new ResponseEntity<Result>(activityService.updateActivity(activity), HttpStatus.OK);
    }

    // 定义一个查询活动的请求
    @PostMapping("/query")
    public ResponseEntity<Result> queryActivity(@RequestBody Activity activity) {
        return new ResponseEntity<Result>(activityService.queryActivity(activity), HttpStatus.OK);
    }
}
