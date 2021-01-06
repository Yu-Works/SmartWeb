package test;

import com.IceCreamQAQ.YuWeb.validation.Max;
import com.IceCreamQAQ.YuWeb.validation.Min;
import com.IceCreamQAQ.YuWeb.validation.NotNull;
import com.IceCreamQAQ.YuWeb.validation.Valid;

@Valid
public class Test {

    @Max(33)
    private Integer i1;
    @NotNull
    public String s2;
    private boolean b3;
    @Min(50)
    private long l4 = 55;

    public Integer getI1() {
        return i1;
    }

    public void setI1(Integer i1) {
        this.i1 = i1;
    }

    public boolean isB3() {
        return b3;
    }

    public void setB3(boolean b3) {
        this.b3 = b3;
    }
}
