package hello.login.web.argumentresolver;

import hello.login.domain.member.Member;
import hello.login.web.SessionConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

//ArgumentResolver
//등록은 WebConfig에

@Slf4j
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    //이것들이 파라미터를 지원하는가
    //supportsParameter() : @Login 애노테이션이 있으면서 Member 타입이면 해당 ArgumentResolver가 사용된다.
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        log.info("supportsParameter 실행");

        //이 파라미터에 Login애노테이션이 있는지 물어봄
        boolean hasLoginAnnotation = parameter.hasParameterAnnotation(Login.class);
        //parameter.getParameterType()는 Member클래스이냐
        boolean hasMemberType = Member.class.isAssignableFrom(parameter.getParameterType());

        //메서드 파라미터에 @Login 이 있는가, @Login 뒤에 Member 객체가 있느냐
        //둘 다 만족하는지, 만족하면 resolveArgument 실행
        return hasLoginAnnotation && hasMemberType;
    }

    //ArgumentResolver가 실행되었을때 어떤 값을 넣어줄것인가
    //resolveArgument() : 컨트롤러 호출 직전에 호출 되어서 필요한 파라미터 정보를 생성해준다.
    //여기서는 세션에 있는 로그인 회원 정보인 member 객체를 찾아서 반환해준다.
    //이후 스프링MVC는 컨트롤러의 메서드를 호출하면서 여기에서 반환된 member 객체를 파라미터에 전달해준다.
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        log.info("resolveArgument 실행");

        //HttpServletRequest가 필요하기 때문에 형변환
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        //true하면 기존에 세션이 없으면 세션이 만들어짐, 의미 없는 세션이 만들어지지 않도록 false
        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        //세션 꺼냄, Member객체
        //세션이 있으면 Member객체 반환,
        //반환된 멤버 HomeController omeLoginV3ArgumentResolver loginMember에 넘어감
        return session.getAttribute(SessionConst.LOGIN_MEMBER);
    }
}
