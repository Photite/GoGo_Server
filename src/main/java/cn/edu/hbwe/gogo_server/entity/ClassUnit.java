package cn.edu.hbwe.gogo_server.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Photite
 */
@Data
@ToString
public class ClassUnit {
    public static ClassUnit EMPTY = new ClassUnit(null, null, null, null,0,0, null, null, "0");

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String teacher;
    private String room;
//    private Range lesson;
    @TableField("lesson_start")
    private int lessonStart;
    @TableField("lesson_end")
    private int lessonEnd;
    @TableField("lesson_type")
    private String lessonType;
    private String code;
    private int dayInWeek;
//    private List<Range> weekAsMinMax;
    @TableField("week_start")
    private int weekStart;
    @TableField("week_end")
    private int weekEnd;
    @TableField("week_type")
    private String weekType;
    private String weekEachLesson;
    @TableField("edu_username")
    private String eduUsername;
    private String openid;
    private String classInfo;


    public ClassUnit(
            @JSONField(name = "name") String name,
            @JSONField(name = "teacher") String teacher,
            @JSONField(name = "room") String room,
            @JSONField(name = "weekEachLesson") String weekEachLesson,
//            @JSONField(name = "lesson") Range lesson,
            @JSONField(name = "lessonStart") int lessonStart,
            @JSONField(name = "lessonEnd") int lessonEnd,
            @JSONField(name = "lessonType") String lessonType,
            @JSONField(name = "code") String code,
            @JSONField(name = "dayInWeek") String dayInWeek
//            @JSONField(name = "kcmc") String name,
//            @JSONField(name = "xm") String teacher,
//            @JSONField(name = "cdmc") String room,
//            @JSONField(name = "zcd") String weekEachLesson,
//            @JSONField(name = "jcs") String lesson,
//            @JSONField(name = "xqj") String dayInWeek
    ) {

        this.name = name;
        this.room = room;
        if (teacher == null) {
            return;
        }
        this.teacher = teacher;
        this.dayInWeek = Integer.parseInt(dayInWeek);
        this.weekEachLesson = weekEachLesson;
        this.lessonStart = lessonStart;
        this.lessonEnd = lessonEnd;
        this.lessonType = lessonType;
//        this.lesson = lesson;
        this.code = code;

//        List<Range> rtn = new ArrayList<>();
//        for (String a : weekEachLesson.split(",")) {
//            a = a.substring(0, a.length() - 1);
//            if (a.contains("-")) {
//                String[] k = a.split("-");
//                int l;
//                FilterType type = FilterType.ALL;
//                try {
//                    l = Integer.parseInt(k[1]);
//                } catch (NumberFormatException e) {
//                    l = Integer.parseInt(k[1].split("周")[0]);
//                    switch (k[1].split("\\(")[1]) {
//                        case "单" -> type = FilterType.SINGULAR;
//                        case "双" -> type = FilterType.EVEN;
//                    }
//                }
//                rtn.add(new Range(Integer.parseInt(k[0]), l, type));
//                continue;
//            }
//            rtn.add(new Range(Integer.parseInt(a), Integer.parseInt(a), FilterType.ALL));
//        }
//        this.weekAsMinMax = rtn;
        List<Range> rtn = new ArrayList<>();
        for (String a : weekEachLesson.split(",")) {
            a = a.substring(0, a.length() - 1);
            if (a.contains("-")) {
                String[] k = a.split("-");
                int l;
                FilterType type = FilterType.ALL;
                try {
                    l = Integer.parseInt(k[1]);
                } catch (NumberFormatException e) {
                    l = Integer.parseInt(k[1].split("周")[0]);
                    switch (k[1].split("\\(")[1]) {
                        case "单" -> type = FilterType.SINGULAR;
                        case "双" -> type = FilterType.EVEN;
                    }
                }
                rtn.add(new Range(Integer.parseInt(k[0]), l, type));
                // Assign the values to the new fields
                this.weekStart = Integer.parseInt(k[0]);
                this.weekEnd = l;
                this.weekType = type.toString();
                continue;
            }
            rtn.add(new Range(Integer.parseInt(a), Integer.parseInt(a), FilterType.ALL));
        }
//        this.weekAsMinMax = rtn;
    }

    public ClassUnit(int lessonStart, String name, String teacher, String room, int lessonEnd, String lessonType, String code, int dayInWeek, int weekStart, int weekEnd, String weekType, String weekEachLesson, String eduUsername, String openid, String classInfo) {
        this.lessonStart = lessonStart;
        this.name = name;
        this.teacher = teacher;
        this.room = room;
        this.lessonEnd = lessonEnd;
        this.lessonType = lessonType;
        this.code = code;
        this.dayInWeek = dayInWeek;
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.weekType = weekType;
        this.weekEachLesson = weekEachLesson;
        this.eduUsername = eduUsername;
        this.openid = openid;
        this.classInfo = classInfo;
    }

    @Getter
    public static class Conflict extends ClassUnit {
        private final List<ClassUnit> conflict;

        public Conflict(List<ClassUnit> conflict) {
            super("冲突课程", null, "点我查看", null, 0,0,null, null, null);
            this.conflict = conflict;
        }
    }


    @Data
    @ToString
    public static class Range {
        private int start;
        private int end;
        private FilterType type;

        public Range(int start, int end, FilterType type) {
            this.start = start;
            this.end = end;
            this.type = type;
        }

        public Range() {

        }

        public static String formatToString(Range r) {
            StringBuilder builder = new StringBuilder();
            builder.append("第").append(r.start);
            if (r.start == r.end) {
                return builder.append("周").toString();
            }
            builder.append("周---第").append(r.end).append("周");
            if (r.getType() != FilterType.ALL) {
                builder.append("(").append(r.getType() == FilterType.SINGULAR ? "单" : "双").append("周)");
            }
            return builder.toString();
        }
    }

    public enum FilterType {
        //单双周
        ALL,
        //单周
        SINGULAR,
        //双周
        EVEN
    }
}
