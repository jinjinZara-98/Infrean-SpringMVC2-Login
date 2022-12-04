package hello.login.domain.member;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;

//저장하고 관리하는 저장소
@Slf4j
@Repository
public class MemberRepository {

    private static Map<Long, Member> store = new HashMap<>(); //static 사용
    private static long sequence = 0L;//static 사용

    //저장
    public Member save(Member member) {
        member.setId(++sequence);
        //회원가입하면 로그 남기기
        log.info("save: member={}", member);
        store.put(member.getId(), member);

        return member;
    }

    //회원 찾기
    public Member findById(Long id) {

        return store.get(id);
    }

//    public Member findByLoginId(String loginId) {
//        List<Member> all = findAll();
//        //값들을 다 갖고와 값들의 아이디와 파라미터로 넘어온 아이디와 같은지
//        for (Member m : all) {
//            if (m.getLoginId().equals(loginId))
//                return m;
//
//        }
//        return null;
//    }

//    public Optional<Member> findByLoginId(String loginId)  {
//        List<Member> all = findAll();
//        //값들을 다 갖고와 값들의 아이디와 파라미터로 넘어온 아이디와 같은지
//        for (Member m : all) {
//            if (m.getLoginId().equals(loginId))
//                return Optional.of(m);
//
//        }
//        return Optional.empty(m);
//    }

    //위와 같은 내용
    //로그인 아이디로 찾는
    //Optional은 회원 객체가 있을 수도 없을 수도 있는
    //값을 null로 반환해야 하는 상황에서는 Optional.empty로 찾을 수 있도록
    //람다로 리스트를 stream()하면 루프를 돈다, .filter db where절 치는거처럼 조건에 만족하는 값만 다음 단계로 넘어감
    //.findFirst() 먼저 나오는 애를 반환
    public Optional<Member> findByLoginId(String loginId) {

        return findAll().stream()
                .filter(m -> m.getLoginId().equals(loginId))
                .findFirst();
    }

    //회원 전체 찾기
    public List<Member> findAll() {

        //map객체에 있는 값들을 리스트로 변환해 반환, 키 빼고 값만 갖고오는
        return new ArrayList<>(store.values());
    }

    public void clearStore() {
        store.clear();
    }
}
