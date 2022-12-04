package hello.login.web;

//우리가 직접 구현한 세션의 개념이 이미 구현되어 있고, 더 잘 구현되어 있다.
//서블릿이 제공하는 HttpSession 도 결국 우리가 직접 만든 SessionManager 와 같은 방식으로 동작
//서블릿을 통해 HttpSession 을 생성하면 다음과 같은 쿠키를 생성한다. 쿠키 이름이 JSESSIONID 이고, 값은 추정 불가능한 랜덤 값
//Cookie: JSESSIONID=5B78E23B513F50164D6FDD8C97B0AD05

//값을 넣었다 빼는 상수값
//생성할 객체가 아닌 글자만 참고하기 때문에 추상클래스로, 생성되지 않게
public abstract class SessionConst {
    public static final String LOGIN_MEMBER = "loginMember";
}
