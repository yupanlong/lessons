package org.geektimes.projects.user.valitador.bean.valitador;

import org.apache.commons.lang.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneValidAnnotationValidator implements ConstraintValidator<Phone, String> {
    private String  message;
    public void initialize(Phone constraint) {
        this.message = constraint.message();
    }

    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (StringUtils.isNotEmpty(phone)) {
            return valid(phone);
        }
        return false;
    }

    private boolean valid(String phone){
        String rex = "^(13[0-9]|14[5|7]|15[0|1|2|3|4|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$";
        Pattern compile = Pattern.compile(rex);
        return compile.matcher(phone).matches();
    }

}
