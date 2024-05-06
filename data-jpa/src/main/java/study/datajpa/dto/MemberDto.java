package study.datajpa.dto;

import lombok.Data;
import study.datajpa.entity.Member;

@Data
public class MemberDto {
    private Long id;
    private String username;
    private String teamName;

    public MemberDto(Long id, String username, String teamName) {
        this.id = id;
        this.username = username;
        this.teamName = teamName;
    }
    // DTO 는 Entity 를 봐도(매개변수로 받아도) 괜찮다. 그 반대는 안 하는 것이 좋음
    public MemberDto(Member member){
        this.id = member.getId();
        this.username = member.getUsername();
    }
}
