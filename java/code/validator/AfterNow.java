package com.technology.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 日期验证器
 */
@Documented
@Constraint(validatedBy = AfterNowValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface AfterNow {

    String message();

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
