package cn.edu.hbwe.gogo_server.controller;

import cn.edu.hbwe.gogo_server.dto.Result;
import cn.edu.hbwe.gogo_server.entity.Reminder;
import cn.edu.hbwe.gogo_server.service.ReminderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reminders")
public class ReminderController {
    @Autowired
    private ReminderService reminderService;

//    @PostMapping
//    public ResponseEntity<Result> createReminder(@RequestBody Reminder reminder) {
////        reminderService.sendReminder(reminder);
////        reminderService.saveReminder(reminder);
//        return new ResponseEntity<>(reminderService.saveReminder(reminder), HttpStatus.OK);
//
//    }

    @PostMapping("/createReminder")
    public void createReminder(@RequestBody Reminder reminder) {
//        reminderService.sendReminder(reminder);
        reminderService.sendReminder(reminder);
//        return new ResponseEntity<>(reminderService.saveReminder(reminder), HttpStatus.OK);

    }

    @GetMapping("/test")
    public ResponseEntity<String> testSendMessage() throws JsonProcessingException {
        reminderService.testSendDelayedMessage();
        return new ResponseEntity<>("Test message sent", HttpStatus.OK);
    }



//    @GetMapping
//    public List<Reminder> getReminders() {
//        // 这里是一个示例，你需要在ReminderService中实现getReminders方法
//        return reminderService.getReminders();
//    }
}