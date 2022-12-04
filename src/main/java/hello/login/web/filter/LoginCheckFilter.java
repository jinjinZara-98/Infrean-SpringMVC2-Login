package hello.login.web.filter;

import hello.login.web.SessionConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.PatternMatchUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

//인증 체크 필터
//로그인 되지 않은 사용자는 상품 관리 뿐만 아니라 미래에 개발될 페이지에도 접근하지 못하도록
@Slf4j
public class LoginCheckFilter implements Filter {

    //체크 안하는 url리스트
    //인증 필터를 적용해도 홈, 회원가입, 로그인 화면, css 같은 리소스에는 접근할 수 있어야 한다. 이렇게
    //화이트 리스트 경로는 인증과 무관하게 항상 허용한다. 화이트 리스트를 제외한 나머지 모든 경로에는 인증 체크 로직을 적용
    private static final String[] whitelist = {"/", "/members/add", "/login", "/logout", "/css/*"};

    //doFilter만 구현
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        //ServletRequest request 는 HTTP 요청이 아닌 경우까지 고려해서 만든 인터페이스
        //HTTP를 사용하면다운캐스팅(형변환), ServletRequest는 기능이 별로 없기 때문에
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            log.info("인증 체크 필터 시작 {}", requestURI);


            if (isLoginCheckPath(requestURI)) {

                //whitelist가 아니면 인증체크 로직 실행
                log.info("인증 체크 로직 실행 {}", requestURI);

                //세션을 찾아, HttpSession에 데이터가 들어있는지 확인
                HttpSession session = httpRequest.getSession(false);

                //세션이 null이거나 로그인한 데이터가 없다면 로그인 안된 것으로 간주
                if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {

                    log.info("미인증 사용자 요청 {}", requestURI);

                    //로그인으로 redirect
                    //whitelist가 아닌 다른url이 들어오면 requestURI는 현재 페이지 정보를 포함해서 넘김
                    //미인증 사용자는 로그인 화면으로 리다이렉트 한다. 그런데 로그인 이후에 다시 홈으로 이동해버리면,
                    //원하는 경로를 다시 찾아가야 하는 불편함이 있다. 예를 들어서 상품 관리 화면을 보려고 들어갔다가
                    //로그인 화면으로 이동하면, 로그인 이후에 다시 상품 관리 화면으로 들어가는 것이 좋다.
                    // 이런 부분이 개발자 입장에서는 좀 귀찮을 수 있어도 사용자 입장으로 보면 편리한 기능이다.
                    // 이러한 기능을 위해 현재 요청한 경로인 requestURI 를 /login 에 쿼리 파라미터로 함께 전달한다.
                    // 물론 login 컨트롤러에서 로그인 성공시 해당 경로로 이동하는 기능은 추가로 개발
                    httpResponse.sendRedirect("/login?redirectURL=" + requestURI);

                    //여기서 리턴하면 다음 서블릿이나 컨트롤러 호출 안하겠다
                    //미인증 사용자는 다음으로 진행하지 않고 끝!
                    //앞서 redirect 를 사용했기 때문에 redirect 가 응답으로 적용되고 요청이 끝난다
                    return;
                }
            }
            //whitelist면 바로 여기로
            //체크해야되는 경로가 아니면 그 다음 필터로 넘어감
            chain.doFilter(request, response);

        } catch (Exception e) {
            //예외 로깅 가능 하지만, 톰캣까지 예외를 보내주어야 함
            //서블릿필터에서 터진 예외가 올라온걸 여기서 먹어버리면
            //정상인것처럼 동작한기 때문
            throw e;

        } finally {
            log.info("인증 체크 필터 종료 {} ", requestURI);
        }

    }

    /**
     * 화이트 리스트의 경우 인증 체크X
     */
    private boolean isLoginCheckPath(String requestURI) {

        //미리 만들어둔 whitelist와 requestURI를 비교하여 매칭되는가
        //매칭되지 않으면 체크대상
        return !PatternMatchUtils.simpleMatch(whitelist, requestURI);
    }
}

//정리
//서블릿 필터를 잘 사용한 덕분에 로그인 하지 않은 사용자는 나머지 경로에 들어갈 수 없게 되었다. 공통
//관심사를 서블릿 필터를 사용해서 해결한 덕분에 향후 로그인 관련 정책이 변경되어도 이 부분만 변경하면 된다.
//필터에는 다음에 설명할 스프링 인터셉터는 제공하지 않는, 아주 강력한 기능이 있는데
//chain.doFilter(request, response); 를 호출해서 다음 필터 또는 서블릿을 호출할 때
// request ,response 를 다른 객체로 바꿀 수 있다. ServletRequest , ServletResponse 를 구현한
// 다른 객체를 만들어서 넘기면 해당 객체가 다음 필터 또는 서블릿에서 사용된다. 잘 사용하는 기능은 아니니 참고

