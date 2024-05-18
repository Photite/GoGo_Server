package cn.edu.hbwe.gogo_server.service;

import cn.edu.hbwe.gogo_server.entity.Reminder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReminderService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendReminder(Reminder reminder) {
        rabbitTemplate.convertAndSend("reminders", reminder);
    }
}