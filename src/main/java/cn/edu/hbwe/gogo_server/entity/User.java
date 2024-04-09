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
    @TableField("wechat_id")
    private String wechatId;
    @TableField("edu_username")
    private String eduUsername;
    @TableField("edu_password")
    private String eduPassword;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

}