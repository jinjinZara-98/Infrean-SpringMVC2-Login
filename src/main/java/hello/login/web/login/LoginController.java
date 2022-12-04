package hello.login.web.login;

import hello.login.domain.login.LoginService;
import hello.login.domain.member.Member;
import hello.login.web.SessionConst;
import hello.login.web.session.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    //자동 의존 주입
    private final LoginService loginService;
    //loginv2에서 사용함
    private final SessionManager sessionManager;

    //url로 login들어왔을때
    @GetMapping("/login")
    public String loginForm(@ModelAttribute("loginForm") LoginForm form) {

        //로그인 화면으로
        return "login/loginForm";
    }

//    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginForm form, BindingResult bindingResult, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return "login/loginForm";
        }

        //로그인한 아이디 비번 맞는지
        Member loginMember = loginService.login(form.getLoginId(), form.getPassword());

        //틀리면
        if (loginMember == null) {
            //reject 글로벌 오류
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");

            //다시 로그인 폼으로
            return "login/loginForm";
        }

        //로그인 성공 처리

        //로그인 버튼을 누르면 웹브라우저랑 서버 사이에서 로그인했다는게 유지가 되어야함
        //서버에서 로그인에 성공하면 HTTP 응답에 쿠키를 담아서 브라우저에 전달하자.
        //그러면 브라우저는 앞으로 해당 쿠키를 지속해서 보내준다.
        //로그인 버튼을 누르면 웹브라우저에서 서버로 전송이 됨
        //서버에서 회원저장소를 뒤져 회원이 있으면 쿠키를 만들어 웹브라우저에 전달, 웹브라우저가 클라이언트
        //이후 다른 페이지에 접근할때 웹브라우저가 쿠키저장소에서 받은 쿠키를 꺼내 전송, 항상
        //쿠키에는 영속 쿠키와 세션 쿠키가 있다.
        //영속 쿠키: 만료 날짜를 입력하면 해당 날짜까지 유지, 컴터 껏다켜도 살아있음
        //세션 쿠키: 만료 날짜를 생략하면 브라우저 종료시 까지만 유지
        //브라우저 종료시 로그아웃이 되길 기대하므로, 우리에게 필요한 것은 세션 쿠키이다

        //로그인에 성공하면 쿠키를 생성하고 HttpServletResponse 에 담는다. 쿠키 이름은 memberId 이고, 값은 회원의 id 를 담아둔다.
        //웹 브라우저는 종료 전까지 회원의 id 를 서버에 계속 보내줄 것이다.
        //서버에서는 쿠키안의 id로 db에서 회원을 찾을수 있음
        //로그인버튼 누르고 F12누르고 Response Header에 Set cookie를 보면 값이 있음
        //이후 뭘눌러도 쿠키값이 넘어와 이 사람이 로그인 됬구나 판단, 아이디르 꺼내 처리
        //쿠키에 시간 정보를 주지 않으면 세션 쿠기(브라우저 종료시 모두 종료)
        //id값을 갖고와 쿠키에 넣음, 쿠키의 파라미터는 문자열이여야 하므로 형변환
        Cookie idCookie = new Cookie("memberId", String.valueOf(loginMember.getId()));
        //생성한 쿠키를 response객체에 넣어야함함
        response.addCookie(idCookie);

        return "redirect:/";
    }

    //세션 적용
//    @PostMapping("/login")
    public String loginV2(@Valid @ModelAttribute LoginForm form, BindingResult bindingResult, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return "login/loginForm";
        }

        Member loginMember = loginService.login(form.getLoginId(), form.getPassword());

        if (loginMember == null) {
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "login/loginForm";
        }

        //로그인 성공 처리

        //세션 관리자를 통해 세션을 생성하고, 회원 데이터 보관, 웹브라우저에 쿠키 넘어감
        sessionManager.createSession(loginMember, response);

        return "redirect:/";

    }

    //우리가 직접 구현한 세션의 개념이 이미 구현되어 있고, 더 잘 구현되어 있다.
    //서블릿이 제공하는 HttpSession 도 결국 우리가 직접 만든 SessionManager 와 같은 방식으로 동작
    //서블릿을 통해 HttpSession 을 생성하면 다음과 같은 쿠키를 생성한다. 쿠키 이름이 JSESSIONID 이고, 값은 추정 불가능한 랜덤 값
    //Cookie: JSESSIONID=5B78E23B513F50164D6FDD8C97B0AD05
    //로그인 버튼 누르면 쿠키에 세션 넣어보내는
//    @PostMapping("/login")
    public String loginV3(@Valid @ModelAttribute LoginForm form, BindingResult bindingResult, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return "login/loginForm";
        }

        Member loginMember = loginService.login(form.getLoginId(), form.getPassword());

        if (loginMember == null) {
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "login/loginForm";
        }

        //로그인 성공 처리
        //request.getSession(true) 세션이 있으면 기존 세션을 반환 기본값이 true
        //세션이 있으면 있는 세션 반환, 없으면 신규 세션을 생성
        //requestSession()는 모든든과정 다 처리줌
        //request.getSession(false)
        //세션이 있으면 기존 세션을 반환
        //세션이 없으면 새로운 세션을 생성하지 않는다. null 을 반환
        HttpSession session = request.getSession();

        //세션에 로그인 회원 정보 보관, 세션에 보관하고 싶은 객체 담아둠
        //session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);
        //세션에 데이터를 보관하는 방법은 request.setAttribute(..) 와 비슷하다. 하나의 세션에 여러 값을 저장 가능
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);

        return "redirect:/";

    }

    //비회원이 페이지에 접근할때 회원인지 체크하고 비회원이면 리다이렉트해서 접근했던 페이지로 다시 넘어가게
    //로그인 화면에서 로그인 버튼 눌렀을때
    @PostMapping("/login")
    public String loginV4(@Valid @ModelAttribute LoginForm form, BindingResult bindingResult,
                          //defaultValue는 redirectURL앞에 붙쳐줌, 기본값
                          // /뒤에 url이 없다면 /로 갈꺼고 그냥 /면 홈 화면이겠쥬? 있다면 /redirectURL로
                          @RequestParam(defaultValue = "/") String redirectURL,
                          HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return "login/loginForm";
        }

        //로그인 폼에 입력한 아이디 비번 맞는지 확인하는
        //입력한 아이디 비번이 맞고 회원이 존재한다면 그 회원 Member 객체 반환
        Member loginMember = loginService.login(form.getLoginId(), form.getPassword());

        //아이디 비번 틀리면
        if (loginMember == null) {
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "login/loginForm";
        }

        //로그인 성공 처리
        //세션이 있으면 있는 세션 반환, 없으면 신규 세션을 생성
        HttpSession session = request.getSession();

        //세션에 로그인 회원 정보 보관
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);

        return "redirect:" + redirectURL;

    }

    //로그아웃버튼을 눌렀을때 쿠키를 날려버리는
//    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        expireCookie(response, "memberId");
        return "redirect:/";
    }

    //세션 적용
//    @PostMapping("/logout")
    public String logoutV2(HttpServletRequest request) {

        //세션 만료
        //쿠키 만든 값을 꺼내 만료
        sessionManager.expire(request);
        return "redirect:/";
    }

    //HttpSession 적용, 로그아웃해도 f12눌러보면 쿠키는 남아있음
    @PostMapping("/logout")
    public String logoutV3(HttpServletRequest request) {
        //세션을 없애는게 목적이므로 false
        HttpSession session = request.getSession(false);

        //session.invalidate() 세션과 그 안에 있는 데이터 다 날라감
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

    //쿠키 날리는 메서드 따로 생성
    private void expireCookie(HttpServletResponse response, String cookieName) {
        //쿠키에 값을 null로 넣고, 시간을 0으로 넣고, Response객체에 추가
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}

//클라이언트가 요청한 sessionId 쿠키의 값으로, 세션 저장소에 보관한 sessionId와 값 제거