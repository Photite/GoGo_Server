package cn.edu.hbwe.gogo_server.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@Data
public class SchoolCalender {
    @TableId(value = "edu_username")
    private String eduUsername;
    private LocalDate start;
    private LocalDate end;
    @TableField("years_of_schooling")
    private String yearsOfSchooling;
    @TableField("semester_number")
    private String semesterNumber;

    public SchoolCalender() {
    }

    public SchoolCalender(String eduUsername, LocalDate start, LocalDate end, String yearsOfSchooling, String semesterNumber) {
        this.eduUsername = eduUsername;
        this.start = start;
        this.end = end;
        this.yearsOfSchooling = yearsOfSchooling;
        this.semesterNumber = semesterNumber;
    }

    public SchoolCalender(LocalDate start, LocalDate end, String yearsOfSchooling, String semesterNumber) {
        this.start = start;
        this.end = end;
        this.yearsOfSchooling = yearsOfSchooling;
        this.semesterNumber = semesterNumber;
    }

}
