package com.techonology.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 组合字段验证器样例
 */
public class OperateGroupAndTypeValidator implements ConstraintValidator<ValidOperateGroupAndType, CutOverOrderUpdateParam> {

    @Override
    public boolean isValid(CutOverOrderUpdateParam value, ConstraintValidatorContext context) {
        if (null == value) {
            return true;
        }

        CutOverOperateGroupEnum groupEnum = CutOverOperateGroupEnum.get(value.getOperateGroup());
        CutOverOperateTypeEnum typeEnum = CutOverOperateTypeEnum.get(value.getOperateType());
        if (null == groupEnum || null == typeEnum) {
            return true;
        }

        return groupEnum.getValue().equals(typeEnum.getGroup());
    }

}
