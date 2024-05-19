package cn.edu.hbwe.gogo_server.service;

import cn.edu.hbwe.gogo_server.dao.ReminderDao;
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;


@Service
public class ReminderService {

    @Autowired
    private ReminderDao reminderDao;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Jackson2JsonMessageConverter producerJackson2MessageConverter;

//    public void sendReminder(Reminder reminder) {
//        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter);
//        rabbitTemplate.convertAndSend("reminders", reminder);
//    }

//    public Result saveReminder(Reminder reminder) {
//        System.out.println("Saved reminder: " + reminder);
//        if (reminderDao.insert(reminder) == 1) {
//            return new Result("提醒保存成功", "1000", reminder);
//        } else {
//            return new Result("提醒保存成功", "2000", null);
//        }
//    }


//    @Scheduled(cron = "0 * * * * ?")  // 每小时执行一次
//    public void checkReminders() {
//        // 查询到期的提醒
//        List<Reminder> expiredReminders = getExpiredReminders();
//
//        // 将到期的提醒放入 RabbitMQ
//        for (Reminder reminder : expiredReminders) {
//            sendReminder(reminder);
//        }
//    }
//
//    private List<Reminder> getExpiredReminders() {
//        // 这里是一个示例，你需要在这里实现查询到期的提醒的逻辑
//        // 可以调用 ReminderDao 的方法来查询数据库
//        return new ArrayList<>();
//    }
//
    public void testSendMessage() {
        // 创建要发送的消息
        String message = "Test message";

        // 发送消息到交换器
        rabbitTemplate.convertAndSend("delay-exchange", "delay.*", message);
    }

    public void testSendDelayedMessage() throws JsonProcessingException {
        // 创建一个Map来存储你的消息
        Map<String, String> messageContent = new HashMap<>();
        messageContent.put("message", "Test delay message");

// 使用ObjectMapper将Map转换为JSON字符串
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonMessage = objectMapper.writeValueAsString(messageContent);

// 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("x-delay", 20000);  // 设置延迟时间为20000毫秒

// 创建要发送的消息
        Message message = new Message(jsonMessage.getBytes(), messageProperties);

// 发送消息到交换器
        rabbitTemplate.convertAndSend("delay-exchange", "delay.*", message);
    }

//    public void sendReminder(Reminder reminder) {
//        try {
//            // 将 Reminder 对象转换为 JSON 字符串
//            String reminderJson = objectMapper.writeValueAsString(reminder);
//
//            // 创建消息属性
//            MessageProperties messageProperties = new MessageProperties();
//            messageProperties.setHeader("x-delay", 20000);  // 设置延迟时间为20000毫秒
//
//            // 创建消息
//            Message message = new Message(reminderJson.getBytes(), messageProperties);
//
//            // 发送消息到队列
//            rabbitTemplate.convertAndSend("delay-exchange", "delay.*", message);
//        } catch (Exception e) {
//            // 处理异常
//            e.printStackTrace();
//        }
//    }

    public void sendReminder(Reminder reminder) {
        try {
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
                return;
            }

            // 创建消息属性
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setHeader("x-delay", (int) delay);

            // 创建消息
            Message message = new Message(reminderJson.getBytes(), messageProperties);

            // 发送消息到队列
            rabbitTemplate.convertAndSend("delay-exchange", "delay.*", message);
        } catch (Exception e) {
            // 处理异常
            e.printStackTrace();
        }
    }
}