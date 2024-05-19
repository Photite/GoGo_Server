package cn.edu.hbwe.gogo_server.entity;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reminder implements Serializable {
    private String id;
    private String openId;
    private String content;
    private LocalDateTime reminderTime;
    private String templateId; // 新增字段

}
