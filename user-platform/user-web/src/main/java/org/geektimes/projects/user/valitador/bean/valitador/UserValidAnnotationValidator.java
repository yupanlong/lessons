package org.geektimes.projects.user.valitador.bean.valitador;

import org.geektimes.projects.user.domain.User;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UserValidAnnotationValidator implements ConstraintValidator<UserValid, User> {
    private int idRange;
    @Override
    public void initialize(UserValid constraintAnnotation) {
        this.idRange = constraintAnnotation.idRange();
    }

    @Override
    public boolean isValid(User value, ConstraintValidatorContext context) {
        String defaultConstraintMessageTemplate = context.getDefaultConstraintMessageTemplate();

        return false;
    }
}
