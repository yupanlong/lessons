package org.geektimes.projects.user.valitador.bean.valitador;

import org.geektimes.projects.user.domain.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class BeanValitadortionDemo {
    public static void main(String[] args) {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        User user = new User();
        user.setPassword("1123123");
        user.setPhoneNumber("15255495339");
        Set<ConstraintViolation<User>> constraintViolationSet = validator.validate(user);
        constraintViolationSet.forEach(cv->{
            System.out.println(cv.getPropertyPath());
            System.out.println(cv.getMessage());
        });
    }
}
