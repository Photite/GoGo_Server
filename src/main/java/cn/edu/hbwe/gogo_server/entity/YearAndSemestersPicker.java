package cn.edu.hbwe.gogo_server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;

/**
 * @author Photite
 */
@Data
@AllArgsConstructor
public class YearAndSemestersPicker {
    private HashMap<String,String> years;
    private HashMap<String,String> semesters;
    private Term defaultTerm;
}
