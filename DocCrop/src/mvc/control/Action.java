package mvc.control;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is an annotation interface, which lets you write @Action by a method
 * to mark it as a method that will be used for runtime actions. 
 * 
 * @author Tomas Toss
 * 
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(value=ElementType.METHOD)
public @interface Action {
}
