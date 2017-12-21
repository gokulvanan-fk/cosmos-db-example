package com.example;

import com.github.javafaker.Faker;

import java.util.Random;
import java.util.UUID;

/**
 * Created by arun.khetarpal on 18/12/17.
 */
public class PersonFactory {

    private static String gender()
    {
        return new Random().nextInt(100) > 50 ? "M" : "F";
    }

    private static Integer favoriteNumber() {return new Random().nextInt(100); }
    public static Person doCreate() {
        Faker generator = new Faker();
        return new Person(UUID.randomUUID().toString(), gender(), generator.finance().creditCard(),
                new Person.Educator(generator.educator().university(), generator.educator().course(),
                        generator.educator().secondarySchool(), generator.educator().campus()),
                favoriteNumber());
    }
}