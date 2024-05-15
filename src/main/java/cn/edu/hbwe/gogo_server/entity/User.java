package cn.edu.hbwe.gogo_server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.*;

/**
 * 用户实体类
 * @author Photite
 */
@Setter
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    private String password;
    @TableField("open_id")
    private String openId;
    @TableField("edu_username")
    private String eduUsername;
    @TableField("edu_password")
    private String eduPassword;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String username, String password, String openId, String eduUsername, String eduPassword) {
        this.username = username;
        this.password = password;
        this.openId = openId;
        this.eduUsername = eduUsername;
        this.eduPassword = eduPassword;
    }
}