package hello.login.web.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

//세션정보를 확인하는 클래스
@Slf4j
//반환값이 논리경로가 아닌 홈페이지에 찍어내는
@RestController
public class SessionInfoController {

    ///session-info 이 url로 들어와야 밑 코드 실행
    @GetMapping("/session-info")
    public String sessionInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        //세션 없으면 "세션이 없습니다." 홈페이지에 출력
        if (session == null) {
            return "세션이 없습니다.";
        }

        //세션 데이터 출력, 보관되어 있는 값들 모두 확인 가능
        session.getAttributeNames().asIterator()
                //배열을 하나씩 넣어 루프 돌림
                .forEachRemaining(name -> log.info("session name={}, value={}", name, session.getAttribute(name)));

        log.info("sessionId={}", session.getId());
        //비활성화시키는 최대 시간
        log.info("getMaxInactiveInterval={}", session.getMaxInactiveInterval());
        //타입이 long이므로 Date타입으로 바꾸기
        log.info("creationTime={}", new Date(session.getCreationTime()));
        //마지막에 접근한 시간
        log.info("lastAccessedTime={}", new Date(session.getLastAccessedTime()));
        //새로운 세션이냐? 원래 있던거냐?, 이미 생성되있는 세션을 써서 false됨
        log.info("isNew={}", session.isNew());

        return "세션 출력";

    }
}
//세션 타임아웃 설정
//세션은 사용자가 로그아웃을 직접 호출해서 session.invalidate() 가 호출 되는 경우에 삭제된다.
//그런데 대부분의 사용자는 로그아웃을 선택하지 않고, 그냥 웹 브라우저를 종료한다.
// 문제는 HTTP가 비연결성(ConnectionLess)이므로 서버 입장에서는 해당 사용자가 웹 브라우저를 종료한 것인지 아닌지를 인식할 수 없다.
//따라서 서버에서 세션 데이터를 언제 삭제해야 하는지 판단하기가 어렵다.
//이 경우 남아있는 세션을 메모리를 사용하므로 무한정 보관하면 다음과 같은 문제가 발생할 수 있다.

//세션과 관련된 쿠키( JSESSIONID )를 탈취 당했을 경우 오랜 시간이 지나도 해당 쿠키로 악의적인 요청을 할 수 있다.
//세션은 기본적으로 메모리에 생성된다. 메모리의 크기가 무한하지 않기 때문에 꼭 필요한 경우만 생성해서 사용해야 한다.
// 10만명의 사용자가 로그인하면 10만개의 세션이 생성되는 것이다.

//세션의 종료 시점
//세션의 종료 시점을 어떻게 정하면 좋을까? 가장 단순하게 생각해보면, 세션 생성 시점으로부터 30분 정도로 잡으면 될 것 같다.
//그런데 문제는 30분이 지나면 세션이 삭제되기 때문에, 열심히 사이트를 돌아다니다가 또 로그인을 해서 세션을 생성해야 한다
//그러니까 30분 마다 계속 로그인해야 하는 번거로움이 발생한다.
//더 나은 대안은 세션 생성 시점이 아니라 사용자가 서버에 최근에 요청한 시간을 기준으로 30분 정도를 유지해주는 것이다.
//이렇게 하면 사용자가 서비스를 사용하고 있으면, 세션의 생존 시간이 30분으로 계속 늘어나게 된다.
// 따라서 30분 마다 로그인해야 하는 번거로움이 사라진다. HttpSession 은 이 방식을 사용한다

//스프링 부트로 글로벌 설정
//application.properties
//server.servlet.session.timeout=60 : 60초, 기본은 1800(30분)

//특정 세션 단위로 시간 설정
//session.setMaxInactiveInterval(1800); //1800초

//세션 타임아웃 발생
//세션의 타임아웃 시간은 해당 세션과 관련된 JSESSIONID 를 전달하는 HTTP 요청이 있으면 현재 시간으로
//다시 초기화 된다. 이렇게 초기화 되면 세션 타임아웃으로 설정한 시간동안 세션을 추가로 사용할 수 있다.
//요청하면 계속 늘어남
//session.getLastAccessedTime() : 최근 세션 접근 시간
//LastAccessedTime 이후로 timeout 시간이 지나면, WAS가 내부에서 해당 세션을 제거

//서블릿의 HttpSession 이 제공하는 타임아웃 기능 덕분에 세션을 안전하고 편리하게 사용할 수 있다.
//실무에서 주의할 점은 세션에는 최소한의 데이터만 보관해야 한다는 점이다. 보통 아이디나 이름 정도
// 보관한 데이터 용량 * 사용자 수로 세션의 메모리 사용량이 급격하게 늘어나서 장애로 이어질 수 있다.
// 추가로 세션의 시간을 너무 길게 가져가면 메모리 사용이 계속 누적 될 수 있으므로 적당한 시간을 선택하는 것이 필요하다.
// 기본이 30 분이라는 것을 기준으로 고민하면 된다