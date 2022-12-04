package hello.login.web;

import hello.login.domain.member.Member;
import hello.login.domain.member.MemberRepository;
import hello.login.web.argumentresolver.Login;
import hello.login.web.session.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberRepository memberRepository;
    private final SessionManager sessionManager;

//    @GetMapping("/")
    public String home() {
        return "home";
    }

    //로그인처리까지 되는 화면
    //쿠키를 받는 방법은 여러가지가 있지만 스프링이 제공하는 @CookieValue
    //로그인 안한 사용자도 들어오게 required = false로
    //id는 스프링이 자동으로 문자열타입으로 형변환해줌
//    @GetMapping("/")
    public String homeLogin(@CookieValue(name = "memberId", required = false) Long memberId, Model model) {

        //id없으면 홈으로 다시 보내기
        if (memberId == null) {
            return "home";
        }

        //로그인
        Member loginMember = memberRepository.findById(memberId);
        //회원을 찾는데 db에 없으면, 쿠키가 옛날에 만들어졌을 경우
        if (loginMember == null) {
            return "home";
        }

        //로그인 성공하면 회원가입버튼이 상품관리, 로그이버튼이 로그아웃으로,
        //객체가 있다면 모델에 담아 loginHome에 반환
        model.addAttribute("member", loginMember);
        return "loginHome";
    }

    //세션 적용
//    @GetMapping("/")
    public String homeLoginV2(HttpServletRequest request, Model model) {

        //세션 관리자에 저장된 회원 정보 조회
        Member member = (Member)sessionManager.getSession(request);

        //로그인, 객체 없으면 홈으로
        //세션 관리자에서 저장된 회원 정보를 조회한다. 만약 회원 정보가 없으면,
        // 쿠키나 세션이 없는 것 이므로 로그인 되지 않은 것으로 처리
        if (member == null) {
            return "home";
        }

        //모델에 Member객체 담아 loginhome으로
        model.addAttribute("member", member);
        return "loginHome";
    }

    //HttpSession적용, 세션을 꺼내주는
//    @GetMapping("/")
    public String homeLoginV3(HttpServletRequest request, Model model) {

        //처음 들어온 사용자도 세션이 만들어짐 그렇기 때문에 false
        //false는 기존에 세션이 없다면 생성하지 않음
        //세션을 만들 의도가 없기 때문에
        HttpSession session = request.getSession(false);

        //세션이 없으면 home으로
        if (session == null) {
            return "home";
        }

        //세션에 맞는 Member객체 가져옴
        Member loginMember = (Member)session.getAttribute(SessionConst.LOGIN_MEMBER);

        //세션에 회원 데이터가 없으면 home
        if (loginMember == null) {
            return "home";
        }

        //세션이 유지되면 로그인으로 이동, 모델에 Member객체 담아
        model.addAttribute("member", loginMember);
        return "loginHome";
    }

    //스프링이 제공하는 @SessionAttribute 사용
    //이미 로그인 된 사용자를 찾을 때는 다음과 같이 사용하면 된다. 참고로 이 기능은 세션을 생성하지 않는다
    //@SessionAttribute(name = "loginMember", required = false) Member loginMember

    //로그인을 처음 시도하면 URL이 다음과 같이 jsessionid 를 포함하고 있는 것을 확인
    //브라우저 랜더링하면서 url F12눌렀을때 location, set-cookie에 다 붙음
    //웹 브라우저가 쿠키를 지원하지 않을 때 쿠키 대신 URL을 통해서 세션을 유지하는 방법
    //그런데 매번 다른 페이지로 갈때마다 url에 붙여주지 않으므로 사용성 별로, 이 방법을 사용하려면 URL에 이 값을 계속 포함해서 전달해야 한다.
    //타임리프 같은 템플릿은 엔진을 통해서 링크를 걸면 jsessionid 를 URL에 자동으로 포함해준다.
    //서버 입장에서 웹 브라우저가 쿠키를 지원하는지 하지 않는지 최초에는 판단하지 못하므로, 쿠키 값도 전달하고, URL에 jsessionid 도 함께 전달
    //URL 전달 방식을 끄고 항상 쿠키를 통해서만 세션을 유지하고 싶으면 다음 옵션
    //application.properties   server.servlet.session.tracking-modes=cookie
//    @GetMapping("/")
    public String homeLoginV3Spring(
            //HttpServletRequest 객체를 써서etSession 할 필요 없이 @SessionAttribute로
            //세션 어트리뷰트 있는지 체크하는 로직이 다 들어있음
            //세션을 한 번에 꺼내는, 세션에 맞는 Member객체 loginMember에 넣음
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember, Model model) {

        //세션에 회원 데이터가 없으면 home
        if (loginMember == null) {
            return "home";
        }

        //세션이 유지되면 로그인으로 이동
        model.addAttribute("member", loginMember);
        return "loginHome";
    }

    //ArgumentResolver사용
    //@Login은 직접 만든 어노테이션
    //@Login 확인하고 넌 로그인된 사용자구나 하고 세션에서 찾아서 넣어주는
    //ArgumentResolver를 만들어 동작방식 바꿔줌
    //조건이 만족했을때 우리가 만든 ArgumentResolver가 동작하게 하는
    //컨트롤러에서 공통으로 사용하는거 애노테이션 하나로 간편하게 해결
    //실행해보면, 결과는 동일하지만, 더 편리하게 로그인 회원 정보를 조회할 수 있다.
    //이렇게 ArgumentResolver 를 활용하면 공통 작업이 필요할 때 컨트롤러를 더욱 편리하게 사용할 수 있다
    //실행하면 HomeController에서 /home으로
    @GetMapping("/")
    public String homeLoginV3ArgumentResolver(@Login Member loginMember, Model model) {

        //세션에 회원 데이터가 없으면 home
        if (loginMember == null) {
            return "home";
        }

        //세션이 유지되면 로그인으로 이동
        model.addAttribute("member", loginMember);
        return "loginHome";
    }
}