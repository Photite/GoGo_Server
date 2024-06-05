package cn.edu.hbwe.gogo_server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@ToString
@Getter
@Setter
public class Articles {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String summary;
    @TableField("cover_image")
    private String coverImage;
    @TableField("public_name")
    private String publicName;
    @TableField("public_avatar")
    private String publicAvatar;
    @TableField("publish_date")
    private java.sql.Timestamp publishDate;
    private String url;

}
