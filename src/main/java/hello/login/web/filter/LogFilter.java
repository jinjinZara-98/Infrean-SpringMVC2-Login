package hello.login.web.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
//필터는 서블릿이, 인터셉터는 스프링이 제공, 둘다 비슷
//요구사항을 보면 로그인 한 사용자만 상품 관리 페이지에 들어갈 수 있어야 한다.
//앞에서 로그인을 하지 않은 사용자에게는 상품 관리 버튼이 보이지 않기 때문에 문제가 없어 보인다.
//그런데 문제는 로그인 하지 않은 사용자도 다음 URL을 직접 호출하면 상품 관리 화면에 들어갈 수 있음
//품 관리 컨트롤러에서 로그인 여부를 체크하는 로직을 하나하나 작성하면 되겠지만, 등록, 수정, 삭제, 조회 등등
//상품관리의 모든 컨트롤러 로직에 공통으로 로그인 여부를 확인해야 한다.
//더 큰 문제는 향후 로그인과 관련된 로직이 변경될 때 이다. 작성한 모든 로직을 다 수정해야 할 수 있다.
//이렇게 애플리케이션 여러 로직에서 공통으로 관심이 있는 있는 것을 공통 관심사
//이러한 공통 관심사는 스프링의 AOP로도 해결할 수 있지만, 웹과 관련된 공통 관심사는
//지금부터 설명할 서블릿 필터 또는 스프링 인터셉터를 사용하는 것이 좋다. 더 부가적인 기능 사용 가능하기 때문
//웹과 관련된 공통 관심사를 처리할 때는 HTTP의 헤더나 URL의 정보들이 필요한데, 서블릿 필터나 스프링 인터셉터는 HttpServletRequest 를 제공\

//필터 흐름
//HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 컨트롤러

//필터는 서블릿이 지원하는 수문장
//HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 컨트롤러
//필터를 적용하면 필터가 호출 된 다음에 서블릿이 호출된다.
//그래서 모든 고객의 요청 로그를 남기는 요구사항이 있다면 필터를 사용하면 된다.
//참고로 필터는 특정 URL 패턴에 적용할 수 있다.  이 url로 들어오면 이 필터를 호출해라
///* 이라고하면 모든 요청에 필터가 적용된다.
//참고로 스프링을 사용하는 경우 여기서 말하는 서블릿은 스프링의 디스패처 서블릿

//필터 제한
//HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 컨트롤러 //로그인 사용자
//HTTP 요청 -> WAS -> 필터(적절하지 않은 요청이라 판단, 서블릿 호출X) //비 로그인 사용자

//필터 체인
//HTTP 요청 -> WAS -> 필터1 -> 필터2 -> 필터3 -> 서블릿 -> 컨트롤러
//필터는 체인으로 구성되는데, 중간에 필터를 자유롭게 추가할 수 있다. 예를 들어서 로그를 남기는 필터를
//먼저 적용하고, 그 다음에 로그인 여부를 체크하는 필터를 만들 수 있다.

//was에서 doFilter 호출, 이거 통과하면 다른 필터 통과한 후 서블릿 호출
//필터 인터페이스를 구현하고 등록하면 서블릿 컨테이너가 필터를 싱글톤 객체로 생성하고, 관리한다.
//init(): 필터 초기화 메서드, 서블릿 컨테이너가 생성될 때 호출된다.
//doFilter(): 고객의 요청이 올 때 마다 해당 메서드가 호출된다. 필터의 로직을 구현하면 된다.
//destroy(): 필터 종료 메서드, 서블릿 컨테이너가 종료될 때 호출된다.
@Slf4j
public class LogFilter implements Filter {

    //초기화
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("log filter init");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("log filter doFilter");

        //ServletRequest request 는 HTTP 요청이 아닌 경우까지 고려해서 만든 인터페이스
        //HTTP를 사용하면다운캐스팅(형변환), ServletRequest는 기능이 별로 없기 때문에
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        //모든 사용자의 요청uri 남기기
        String requestURI = httpRequest.getRequestURI();

        //요청온걸 구분하기 위해서
        String uuid = UUID.randomUUID().toString();

        try {
            //모든 요청을 로그로 남기기
            log.info("REQUEST [{}][{}]", uuid, requestURI);

            //이 부분이 가장 중요하다. 다음 필터가 있으면 필터를 호출하고, 필터가 없으면 서블릿, 컨트롤러를 호출한다
            //1순위인 LogFilter log.info("REQUEST [{}][{}]", uuid, requestURI) 호출되고
            //chain.doFilter(request, response)로 2순위인 LoginCheckFilter가 실행됨
            //그 다음 log.info("RESPONSE [{}][{}]", uuid, requestURI)
            //만약 이 로직을 호출하지 않으면 다음 단계로 진행되지 않는다
            //다음 필터 호출, 필터 없으면 서블릿 호출됨
            //에러가 나면 컨트롤러에서 에러 로그 남김
            chain.doFilter(request, response);

        } catch (Exception e) {
            throw e;

        } finally {
            //다 끝나고 응답 로그
            log.info("RESPONSE [{}][{}]", uuid, requestURI);
        }

    }

    @Override
    public void destroy() {
        log.info("log filter destroy");
    }
}
