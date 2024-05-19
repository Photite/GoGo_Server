package cn.edu.hbwe.gogo_server.service;

import cn.edu.hbwe.gogo_server.entity.Reminder;
import cn.edu.hbwe.gogo_server.utils.WXUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
public class ReminderListenerService {

    @Autowired
    private WXUtil wxUtil;

    @RabbitListener(queues = "delay-queue")
    public void handleReminder(Reminder reminder) {
        // 处理接收到的提醒
        System.out.println("Received reminder: " + reminder);
        System.out.println("Current time: " + Instant.now());
        String page = "pages/index/main";
        ObjectMapper objectMapper = new ObjectMapper();

        String jsonString = reminder.getContent();

        try {
            Map<String, Map<String, String>> data01 = objectMapper.readValue(jsonString, Map.class);
            System.out.println(data01);
            wxUtil.sendSubscribeMessage(reminder.getOpenId(), reminder.getTemplateId(), page, data01);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}