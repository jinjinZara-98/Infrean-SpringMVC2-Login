package hello.login.web.session;

import hello.login.domain.member.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.*;

class SessionManagerTest {

    //SessionManager객체 생성
    SessionManager sessionManager = new SessionManager();

    @Test
    void sessionTest() {

        //세션 생성
        //HttpServletResponse는 인터페이스,
        //가짜로 테스트할때는 MockHttpServletResponse
        //웹브라우저에 응답이 나갔다고 가정, 서버에서 웹브라우저로, 서버에서 쿠키와 세션을 만들어 response에 담아둠
        MockHttpServletResponse response = new MockHttpServletResponse();
        Member member = new Member();

        //키 값으로 세션 Map객체에 넣어줌
        sessionManager.createSession(member, response);

        //여기서부턴 웹브라우저로 가정, 클라이언트에서 서버로
        //요청에 응답 쿠키 가져와 저장, createSession으로 response에 쿠키 저장해놨으니
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(response.getCookies());

        //다시 클라이언트에서 서버로 요청이 왔을때 세션이 조회가 되는지,
        //Member객체 반환해 이전에 생성한 Member객체와 동일한지
        //세션 조회
        Object result = sessionManager.getSession(request);
        assertThat(result).isEqualTo(member);

        //세션 만료
        sessionManager.expire(request);
        Object expired = sessionManager.getSession(request);
        //값이 없는지 확인
        assertThat(expired).isNull();
    }
}
