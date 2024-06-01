package cn.edu.hbwe.gogo_server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.ToString;

/**
 * @author Photite
 */

@Data
@ToString
public class Activity {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String teacher;
    private String room;
    @TableField("lesson_start")
    private int lessonStart;
    @TableField("lesson_end")
    private int lessonEnd;
    @TableField("lesson_type")
    private String lessonType;
    private String code;
    private int dayInWeek;
    @TableField("week_start")
    private int weekStart;
    @TableField("week_end")
    private int weekEnd;
    @TableField("week_type")
    private String weekType;
    private String weekEachLesson;
    @TableField("edu_username")
    private String eduUsername;
    private String openid;
    private String activityInfo;

}