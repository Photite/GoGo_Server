package cn.edu.hbwe.gogo_server.service;

import cn.edu.hbwe.gogo_server.dao.ActivityDao;
import cn.edu.hbwe.gogo_server.dto.Result;
import cn.edu.hbwe.gogo_server.entity.Activity;
import cn.edu.hbwe.gogo_server.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivityService {

    @Autowired
    private ActivityDao activityDao;

    // 生成一个添加活动的方法
    public Result addActivity(Activity activity) {
        // 调用activityDao的insert方法，将activity对象插入到数据库中
        int i = activityDao.insert(activity);
        // 如果插入成功，返回成功信息
        if (i > 0) {
            return new Result("添加成功", "1000", null);
        } else {
            // 如果插入失败，返回失败信息
            return new Result("添加失败", "2000", null);
        }
    }

    // 生成一个删除活动的方法
    public Result deleteActivity(Activity activity) {
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", activity.getId());
        int i = activityDao.delete(queryWrapper);
        // 如果删除成功，返回成功信息
        if (i > 0) {
            return new Result("删除成功", "1000", null);
        } else {
            // 如果删除失败，返回失败信息
            return new Result("删除失败", "2000", null);
        }
    }

    // 生成一个修改活动的方法
    public Result updateActivity(Activity activity) {
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", activity.getId());
        int i = activityDao.update(activity, queryWrapper);
        // 如果修改成功，返回成功信息
        if (i > 0) {
            return new Result("修改成功", "1000", null);
        } else {
            // 如果修改失败，返回失败信息
            return new Result("修改失败", "2000", null);
        }
    }

    // 生成一个查询活动的方法
    public Result queryActivity(Activity activity) {
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", activity.getId());
        Activity activity1 = activityDao.selectOne(queryWrapper);
        // 如果查询成功，返回成功信息
        if (activity1 != null) {
            return new Result("查询成功", "1000", activity1);
        } else {
            // 如果查询失败，返回失败信息
            return new Result("查询失败", "2000", null);
        }
    }


}
