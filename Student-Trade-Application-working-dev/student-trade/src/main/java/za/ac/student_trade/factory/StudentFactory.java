package za.ac.student_trade.factory;

import za.ac.student_trade.domain.Residence;
import za.ac.student_trade.domain.Student;
import za.ac.student_trade.util.Helper;

public class StudentFactory {

    public static Student createStudent(String firstName, String lastName, String email, String password, Residence residence) {

        if(Helper.isNullOrEmpty(firstName) || Helper.isNullOrEmpty(lastName) || Helper.isNullOrEmpty(email) || Helper.isNullOrEmpty(password)){
            return null;
        }else{
            if(Helper.isValidEmail(email)) {
                String studentId = Helper.generateId();
                return new Student.Builder()
                        .setStudentId(studentId)
                        .setFirstName(firstName)
                        .setLastName(lastName)
                        .setEmail(email)
                        .setPassword(password)
                        .setResidence(residence)
                        .build();
            }
            return  null;
        }   }
}
