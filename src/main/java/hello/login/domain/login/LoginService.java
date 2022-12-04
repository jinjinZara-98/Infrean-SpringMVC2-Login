package hello.login.domain.login;

import hello.login.domain.member.Member;
import hello.login.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

//로그인 판단 로직
//아이디 비번이 맞는지
@Service
@RequiredArgsConstructor
public class LoginService {

    //자동 의존 주입
    private final MemberRepository memberRepository;

    /**
     * @return null 로그인 실패
     */
    public Member login(String loginId, String password) {

        //로그인 아이디를 던져 회원이 있는지 찾음
        //꺼낼 때는 get()으로, 없으면 예외 터짐
//        Optional<Member> findMemberOptional = memberRepository.findByLoginId(loginId);
//        Member member = findMemberOptional.get();
//        //그 아이디에 맞는 비번봐 로그인 할때 들어온 비번과 같으면 객체 반환
//        if(member.getPassword().equals(password)) {
//            return member;
//        } else{
//            return null;
//        }

        return memberRepository.findByLoginId(loginId)
                .filter(m -> m.getPassword().equals(password))
                .orElse(null);
    }
}
