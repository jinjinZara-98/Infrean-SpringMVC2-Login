package hello.login.web.member;

import hello.login.domain.member.Member;
import hello.login.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

//반환값 논리경로
@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    //자동 의존 주입
    private final MemberRepository memberRepository;

    //회원가입 폼으로
    @GetMapping("/add")
    public String addForm(@ModelAttribute("member") Member member) {

        return "members/addMemberForm";
    }

    //회원가입 폼에서 회원가입버튼을 눌렀을 때
    @PostMapping("/add")
    public String save(@Valid @ModelAttribute Member member, BindingResult bindingResult) {

        //에러가 있으면 다시 회원가입 폼으로 넘기기
        if (bindingResult.hasErrors()) {
            return "members/addMemberForm";
        }

        //저장메서드 호출해 로그 남김
        memberRepository.save(member);

        //저장하고 홈 화면으로 리다이렉트
        return "redirect:/";
    }
}
