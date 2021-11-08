package com.techonology.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 验证组合字段样例
 */
@Documented
@Constraint(validatedBy = OperateGroupAndTypeValidator.class)
@Target({ TYPE })
@Retention(RUNTIME)
public @interface ValidOperateGroupAndType {

    String message() default "割接分组与类型不匹配";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
