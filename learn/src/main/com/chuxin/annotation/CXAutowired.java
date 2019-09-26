package chuxin.annotation;

import java.lang.annotation.*;

/**
 * @program: learn
 * @description
 * @author: weis
 * @create: 2019-09-18 19:00
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CXAutowired {
    String value() default "";
}
