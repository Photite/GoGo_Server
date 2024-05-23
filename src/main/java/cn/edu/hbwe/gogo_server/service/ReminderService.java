package cn.edu.hbwe.gogo_server.service;

import cn.edu.hbwe.gogo_server.dao.ReminderDao;
import cn.edu.hbwe.gogo_server.dto.Result;
import cn.edu.hbwe.gogo_server.entity.Reminder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


@Service
public class ReminderService {

    @Autowired
    private ReminderDao reminderDao;

    @Autowired
    private UserService userService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Jackson2JsonMessageConverter producerJackson2MessageConverter;

    public Result sendReminder(Reminder reminder) {
        try {
            // 将reminder中openid部分取出，使用findOpenidByEduUsername方法查询到真正的openid后再存入reminder中
            String openid = reminder.getOpenId();
            reminder.setOpenId(userService.findOpenidByEduUsername(openid));

//            ZonedDateTime dateTime = ZonedDateTime.parse(reminder.getReminderTime() + " Asia/Shanghai");
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//            String formattedDateTime = dateTime.format(formatter);


            // 将 Reminder 对象转换为 JSON 字符串
            String reminderJson = objectMapper.writeValueAsString(reminder);

            ZonedDateTime nowInSameZone = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"));

            long delay = reminder.getReminderTime()
                    .atZone(ZoneId.of("Asia/Shanghai"))
                    .toInstant()
                    .toEpochMilli() - nowInSameZone.toInstant().toEpochMilli();

            System.out.println("Reminder time: " + reminder.getReminderTime().atZone(ZoneId.of("Asia/Shanghai")));
            System.out.println("Current time: " + nowInSameZone);
            System.out.println("Calculated delay: " + delay);

            if (delay <= 0) {
                // 如果延迟时间小于或等于0，那么打印一条错误消息并返回
                System.err.println("Error: reminderTime is in the past or now. The message will not be delayed.");
                return new Result("提醒时间已过或为当前时间，消息不会被延迟", "1000", null);
            }

            // 创建消息属性
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setHeader("x-delay", (int) delay);

            // 创建消息
            Message message = new Message(reminderJson.getBytes(), messageProperties);

            // 发送消息到队列
            rabbitTemplate.convertAndSend("delay-exchange", "delay.*", message);

            return new Result("提醒消息发送成功", "1000", reminder);
        } catch (Exception e) {
            // 处理异常
            e.printStackTrace();
            return new Result("提醒消息发送失败", "2000", null);
        }
    }
}