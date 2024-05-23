package cn.edu.hbwe.gogo_server.controller;

import cn.edu.hbwe.gogo_server.dto.Result;
import cn.edu.hbwe.gogo_server.entity.Reminder;
import cn.edu.hbwe.gogo_server.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/reminders")
public class ReminderController {
    @Autowired
    private ReminderService reminderService;

    @PostMapping("/createReminder")
    public ResponseEntity<Result> createReminder(@RequestBody Reminder reminder) {
        return new ResponseEntity<Result>(reminderService.sendReminder(reminder), HttpStatus.OK);

    }

//    @PostMapping("/createReminder")
//    public void createReminder(@RequestBody Reminder reminder) {
//        reminderService.sendReminder(reminder);
//    }

//    @GetMapping("/test")
//    public ResponseEntity<String> testSendMessage() throws JsonProcessingException {
//        reminderService.testSendDelayedMessage();
//        return new ResponseEntity<>("Test message sent", HttpStatus.OK);
//    }

}