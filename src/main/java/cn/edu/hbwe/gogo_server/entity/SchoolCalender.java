package cn.edu.hbwe.gogo_server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class SchoolCalender {
    private LocalDate start;
    private LocalDate end;
    private Term currentTerm;
}
