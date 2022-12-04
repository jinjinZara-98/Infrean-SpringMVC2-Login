package hello.login.web.session;

import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

//보안 문제
//쿠키 값은 임의로 변경할 수 있다.
//클라이언트가 쿠키를 강제로 변경하면 다른 사용자가 된다.
//실제 웹브라우저 개발자모드 Application Cookie 변경으로 확인
//Cookie: memberId=1 Cookie: memberId=2 (다른 사용자의 이름이 보임)
//쿠키에 보관된 정보는 훔쳐갈 수 있다.
//만약 쿠키에 개인정보나, 신용카드 정보가 있다면?
//이 정보가 웹 브라우저에도 보관되고, 네트워크 요청마다 계속 클라이언트에서 서버로 전달된다.
//쿠키의 정보가 나의 로컬 PC가 털릴 수도 있고, 네트워크 전송 구간에서 털릴 수도 있다.
//해커가 쿠키를 한번 훔쳐가면 평생 사용할 수 있다.
//해커가 쿠키를 훔쳐가서 그 쿠키로 악의적인 요청을 계속 시도할 수 있다.
//대안
//쿠키에 중요한 값을 노출하지 않고, 사용자 별로 예측 불가능한 임의의 토큰(랜덤 값)을 노출하고,
//추적이 가능한 값 사용 X
//서버에서 토큰과 사용자 id를 매핑해서 인식한다. 그리고 서버에서 토큰을 관리한다.
//토큰은 해커가 임의의 값을 넣어도 찾을 수 없도록 예상 불가능 해야 한다.
//해커가 토큰을 털어가도(예상 불가능하고 복잡한 값을) 시간이 지나면 사용할 수 없도록 서버에서 해당 토큰의 만료시간을 짧게(예: 30분)
//유지한다. 또는 해킹이 의심되는 경우 서버에서 해당 토큰을 강제로 제거하면 된다.
//제거까지는 할 필요없고 짧게 유지까지는 해줘야

//이 문제를 해결하려면 결국 중요한 정보를 모두 서버에 저장해야 한다.
//그리고 클라이언트와 서버는 추정 불가능한 임의의 식별자 값으로 연결
//서버에 중요한 정보를 보관하고 연결을 유지하는 방법을 세션
//클라이언트에서 보낸 아이디 비번이 맞을 경우 세션 저장소에서 추정 불가능한 세션ID를 토큰키로 씀
//값은 회원객체로 쓰고, 세션ID를 알면 회원객체를 꺼내 사용할 수 있는
//이 후 웹브라우저에 쿠키를 넘겨줄때 값을 추정 불가능한 세션ID를 넘김, 그리고 쿠키저장소에 쿠키를 저장해둠
//여기서 중요한 포인트는 회원과 관련된 정보는 전혀 클라이언트에 전달하지 않는다는 것이다.
//오직 추정 불가능한 세션 ID만 쿠키를 통해 클라이언트에 전달, 사용자에 대한 정보 추정불가
//이제 다른 페이지에 접근할때 웹브라우저가 쿠키저장소에서 받은 쿠키를 꺼내 전송하는데
//추정 불가능한 세션 ID가 들어있는 쿠키를 전달하므로 서버에 세션 저장소에서 그 쿠키와 맞는 값을 찾아 조회
//세션을 사용해서 서버에서 중요한 정보를 관리하게 되었다. 덕분에 다음과 같은 보안 문제들을 해결할 수 있다.
//쿠키 값을 변조 가능, 예상 불가능한 복잡한 세션Id를 사용한다.
//쿠키에 보관하는 정보는 클라이언트 해킹시 털릴 가능성이 있다. 세션Id가 털려도 여기에는 중요한 정보가 없다.
//쿠키 탈취 후 사용 해커가 토큰을 털어가도 시간이 지나면 사용할 수 없도록 서버에서 세션의 만료시간을 짧게(예: 30분) 유지한다.
//30분단위로 루프를 돌며 사용안하는 세션을 지움
//또는 해킹이 의심되는 경우 서버에서 해당 세션을 강제로 제거하면 된다

//세션이라는 것이 뭔가 특별한 것이 아니라 단지 쿠키를 사용하는데, 서버에서 데이터를 유지하는 방법
//프로젝트마다 이러한 세션 개념을 직접 개발하는 것은 상당히 불편. 그래서 서블릿도 세션 개념을 지원
/**
 * 세션 관리
 */
@Component
public class SessionManager {

    public static final String SESSION_COOKIE_NAME = "mySessionId";
    //세션 아이디, 값
    //값은 Member객체를 넣을거임
    //동시에 여러 스레드가 접근할때 ConcurrentHashMap
    private Map<String, Object> sessionStore = new ConcurrentHashMap<>();

    /**
     * 세션 생성
     * //세션 생성
     * //sessionId 생성 (임의의 추정 불가능한 랜덤 값)
     * //세션 저장소에 sessionId와 보관할 값 저장
     * //sessionId로 응답 쿠키를 생성해서 클라이언트에 전달
     */
    public void createSession(Object value, HttpServletResponse response) {

        //세션 id(확실한 랜덤값 생성)를 생성하고 키로 파라미터로 들어온건 값으로 세션에 저장,
        String sessionId = UUID.randomUUID().toString();
        sessionStore.put(sessionId, value);

        //쿠키 생성, 쿠키이름은 위에서 만든 필드, 값은 위에서 만든 세션 id
        Cookie mySessionCookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
        //response객체에 쿠키 추가
        response.addCookie(mySessionCookie);
    }

    /**
     * 세션 조회
     * //클라이언트가 요청한 sessionId 쿠키의 값으로, 세션 저장소에 보관한 값 조회
     */
    public Object getSession(HttpServletRequest request) {

        //Http요청으로 들어온걸 객체로 만들어 그 객체의 쿠키이름이 SESSION_COOKIE_NAME인지
        Cookie sessionCookie = findCookie(request, SESSION_COOKIE_NAME);

        if (sessionCookie == null) {
            return null;
        }

        //sessionCookie.getValue()는 추정 불가능한 session id
        //그 session id에 맞는 Member객체 반환
        return sessionStore.get(sessionCookie.getValue());
    }

    /**
     * 세션 만료
     */
    public void expire(HttpServletRequest request) {
        //세션 조회와 똑같이 요청으로 들어온 쿠키 이름이 SESSION_COOKIE_NAME와 같다면
        //그 쿠키를 가져와서 Map객체에서 제거해줌
        Cookie sessionCookie = findCookie(request, SESSION_COOKIE_NAME);

        if (sessionCookie != null) {
            sessionStore.remove(sessionCookie.getValue());
        }
    }

    //요청이랑 쿠키이름을 넣으면 쿠키를 자동으로 찾아주는
    public Cookie findCookie(HttpServletRequest request, String cookieName) {

        //(request.getCookies()는 배열을 반환
        //쿠키가 없다면 null반환
        if (request.getCookies() == null) {
            return null;
        }

        //Arrays.stream는 배열을 스트림으로 바꿔주는
        //요청에서 쿠키를 얻어오고 파라미터로 들어온 쿠키이름과 같은지 비교
        //findFirst는 순서중에 먼저 나온 하나, findAny()는 순서와 상관없이 하나가 찾아지면 반환
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findAny()
                .orElse(null);
    }

}
