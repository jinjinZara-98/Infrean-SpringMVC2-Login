package hello.login.web.interceptor;

import hello.login.web.SessionConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//서블릿 필터로 했던걸 스프링 인터셉터로 다시 만드는 클래스
//서블릿 필터와 비교해서 코드가 매우 간결하다. 인증이라는 것은 컨트롤러 호출 전에만 호출되면 된다.
//서블릿 필터와 스프링 인터셉터는 웹과 관련된 공통 관심사를 해결하기 위한 기술이다.
//서블릿 필터와 비교해서 스프링 인터셉터가 개발자 입장에서 훨씬 편리하다는 것을 코드로 이해했을 것이다.
//특별한 문제가 없다면 인터셉터를 사용하는 것이 좋다
//따라서 preHandle 만 구현
@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {

    //로그인체크는 이거만 있으면 된다
    //whitelist 만들어 주지 않고 인터셉터 등록할때 다 할 수 있음
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //요청에서 사용자 uri가져오고
        String requestURI = request.getRequestURI();

        log.info("인증 체크 인터셉터 실행 {}", requestURI);

        //요청에서 세션 가져오고
        HttpSession session = request.getSession();

        //세션이 null이거나 로그인한 데이터가 없다면 로그인 안된 것으로 간주
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            log.info("미인증 사용자 요청");

            //로그인으로 redirect
            //whitelist가 아닌 다른url이 들어오면 requestURI는 현재 페이지 정보를 포함해서 넘김
            //미인증 사용자는 로그인 화면으로 리다이렉트 한다. 그런데 로그인 이후에 다시 홈으로 이동해버리면,
            //원하는 경로를 다시 찾아가야 하는 불편함이 있다. 예를 들어서 상품 관리 화면을 보려고 들어갔다가
            //로그인 화면으로 이동하면, 로그인 이후에 다시 상품 관리 화면으로 들어가는 것이 좋다.
            // 이런 부분이 개발자 입장에서는 좀 귀찮을 수 있어도 사용자 입장으로 보면 편리한 기능이다.
            // 이러한 기능을 위해 현재 요청한 경로인 requestURI 를 /login 에 쿼리 파라미터로 함께 전달한다.
            // 물론 login 컨트롤러에서 로그인 성공시 해당 경로로 이동하는 기능은 추가로 개발
            //로그인하고 다시 그 페이지로 돌아가도록
            response.sendRedirect("/login?redirectURL=" + requestURI);

            //여기서 리턴하면 다음 서블릿이나 컨트롤러 호출 안하겠다
            //미인증 사용자는 다음으로 진행하지 않고 끝!
            //앞서 redirect 를 사용했기 때문에 redirect 가 응답으로 적용되고 요청이 끝난다
            //문제가 되면 false
            return false;
        }
        //정상적이면 true
        return true;
    }
}
