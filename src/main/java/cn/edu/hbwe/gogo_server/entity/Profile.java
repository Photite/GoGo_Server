package cn.edu.hbwe.gogo_server.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Photite
 */
@Data
@AllArgsConstructor
public class Profile {
    //学号
    @TableId(value = "edu_username")
    private String eduUsername;
    //姓名
    private String name;
    //年级
    private String grade;
    //学院
    @TableField("college_name")
    private String collegeName;
    //专业
    @TableField("study_name")
    private String studyName;
    //身份证
    @TableField("id_card")
    private String idCard;
    //gpa
    @TableField("gpa")
    private String gpa;


    //邮箱
    @TableField(exist = false)
    private String email;
    //手机
    @TableField(exist = false)
    private String phone;
    //政治面貌
    @TableField(exist = false)
    private String policy;
    //外语语种
    @TableField(exist = false)
    private String language;

    public Profile(String eduUsername, String name, String grade, String collegeName, String studyName, String idCard) {
        this.eduUsername = eduUsername;
        this.name = name;
        this.grade = grade;
        this.collegeName = collegeName;
        this.studyName = studyName;
        this.idCard = idCard;
    }

    public Profile(String eduUsername, String name, String grade, String collegeName, String studyName, String idCard, String gpa) {
        this.eduUsername = eduUsername;
        this.name = name;
        this.grade = grade;
        this.collegeName = collegeName;
        this.studyName = studyName;
        this.idCard = idCard;
        this.gpa = gpa;
    }
}
