package cn.edu.hbwe.gogo_server.entity;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reminder {
    private String id;
    private String openId;
    private String content;
    private LocalDateTime reminderTime;
    private String templateId; // 新增字段

}
