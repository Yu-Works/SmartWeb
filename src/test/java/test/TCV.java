package test;

import com.IceCreamQAQ.YuWeb.validation.*;

import javax.inject.Inject;
import java.lang.annotation.Annotation;

public class TCV extends ClassValidatorBase {

    private ValidateData i1vd;
    private ValidateData s2vd;

    @Inject
    public TCV(ValidatorFactory factory) {
        super(factory, test.Test.class, new String[]{"l4"});
        i1vd = JavaUtil.getValidatorData(factory, getClass(), "i1", com.IceCreamQAQ.YuWeb.validation.Min.class);
        s2vd = JavaUtil.getValidatorData(factory, getClass(), "s2", com.IceCreamQAQ.YuWeb.validation.NotNull.class);
    }

    @Override
    public ValidateResult validate(Annotation annotation, Object bean) {
        test.Test b = (test.Test) bean;
        ValidateResult result;
        result = i1vd.getValidator().validate(i1vd.getAnnotation(), b.getI1());
        if (result != null) return result;
        result = s2vd.getValidator().validate(i1vd.getAnnotation(), b.s2);
        if (result != null) return result;
        return reflectValidate(bean);
    }
}
