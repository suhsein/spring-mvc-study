package study.datajpa.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id){
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }


    @GetMapping("/members2/{id}")
    public String findMember2(@PathVariable("id") Member member){
        return member.getUsername();
    }

    /**
     * @PageableDefault 어노테이션으로 페이징 설정 가능.
     *
     * 페이징은 항상 0번 인덱스부터 시작함. 시작을 1번 인덱스로 바꾸려면,
     *
     * 방법 1. PageRequest(Pageable 구현체)를 생성해 리포지토리에 넘기기. 응답값 Page 도 직접 만들어서 제공해야 함.
     * 방법 2. application.properties 에서 one-indexed-parameters: true 로 설정. (단지 page 파라미터를 -1 해서 요청하는 것일 뿐임.)
     *
     * ※주의※ Entity 외부 노출 위험함
     *  페이징 시에도 반드시 DTO 로 변환해서 반환
     */
    @GetMapping("/members")
    public Page<MemberDto> list(@PageableDefault(size = 5, sort = "username", direction = Sort.Direction.DESC) Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(MemberDto::new);
    }

    @PostConstruct
    public void init() {
        for (int i = 0; i < 100; i++) {
            memberRepository.save(new Member("user" + i, i));
        }
    }
}
