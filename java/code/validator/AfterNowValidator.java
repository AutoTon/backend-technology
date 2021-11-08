package com.technology.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Date;

/**
 * 验证日期字段必须在当前时间之后
 */
public class AfterNowValidator implements ConstraintValidator<AfterNow, Date> {

    @Override
    public boolean isValid(Date value, ConstraintValidatorContext context) {
        if (null == value) {
            return true;
        }

        Date now = new Date();
        return value.after(now);
    }

}
