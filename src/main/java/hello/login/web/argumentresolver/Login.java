package hello.login.web.argumentresolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//@Target(ElementType.PARAMETER) : 파라미터에만 사용
//@Retention(RetentionPolicy.RUNTIME) : 리플렉션 등을 활용할 수 있도록 런타임까지 애노테이션 정보가 남아있음
//파라미터에 넣을거라는 의미
@Target(ElementType.PARAMETER)
//실제 동작할때까지 애노테이션이 남아있어야 하므로
@Retention(RetentionPolicy.RUNTIME)
public @interface Login {
}
