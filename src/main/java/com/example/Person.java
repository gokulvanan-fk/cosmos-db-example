package com.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by arun.khetarpal on 18/12/17.
 */
@Getter
@Setter
@AllArgsConstructor
public class Person {
    private String id;
    private String gender;
    private String CreditCard;
    private Educator educator;
    private Integer favoriteNumber;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Educator {
        private String University;
        private String Course;
        private String SecondaySchool;
        private String Campus;
    }
}
