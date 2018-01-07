package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static Integer favoriteNumber() {return new Random().nextInt(10000); }
    
    public static Person doCreate() {
        Faker generator = new Faker();
        //Added to make the payload 1KB in size
        String blob = "sasafdsfasdfasdfaasfdsalksjdlkfklqwerlnklnm,sfndsklnfklskjflksadjfkldsjflsdjafksdjfklsjafkldjsfkljsalkfjdsklfjsdkljdsanfkldsnflkdsanflkdnsflkgskldfjdskljqklklklwejrklewjrewklj;kjksdfjdksljfkejkljsdkfjdsklfjdskljfkldsjfkljasd;mwenrksdfnsdafansdkfafadsasdfsdafadsfdsfjklasdjfkldjklqwjekrjewqklrjeqwklrjewqklrjewsdfkljklajsfkljdsklfjasdklfjdsklfjasdl;sjalksdfj;weqjrekwlrknvkfsgqowjerkwenfkcwejrsfasldqklrjeqklwrjqwklrjweklqjrwkleqjrkwejrkqwjrkwqjerklwejrklqwjerklewjrklewjrklewqjrklqwejrjfladksjflkdsjsjdfksajdfkladsjfkldsjfklakaaaaaaaaasjfdksfjadklsfjkldsjfklewjfqiowejfeklwflksfklasdfkdsfjsdakljfklsadjfaklsdjfklsadfjsadklfklsadjfdklsjfkldsjfklsdjfklasdjflksadjfsdjsdlkfjklsjfasfjdskwioerewfdslaljdksfjdskljfdsjfklsdjktestsfsdfsdfdsfdsfdsfdsdfdsfdsfadsfasdfds"; 
        
        return new Person(UUID.randomUUID().toString(), gender(), generator.finance().creditCard(),
                new Person.Educator(generator.educator().university(), generator.educator().course(),
                        generator.educator().secondarySchool(), generator.educator().campus()),
                favoriteNumber(),blob);
    }
    
    
    
    public static void main(String[] args) throws JsonProcessingException{
        ObjectMapper mapper = new ObjectMapper();
        byte[] data = mapper.writeValueAsBytes(PersonFactory.doCreate());
        System.out.println(data.length); //verify size
    }
}