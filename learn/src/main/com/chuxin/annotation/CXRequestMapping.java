package chuxin.annotation;

import java.lang.annotation.*;

/**
 * @program: learn
 * @description
 * @author: weis
 * @create: 2019-09-18 19:00
 **/
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CXRequestMapping {
    String value() default "";
}
