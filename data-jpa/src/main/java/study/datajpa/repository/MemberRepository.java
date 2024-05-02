package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    // 상위 3개 결과, By 뒤에 조건 없으면 where 문 X
    List<Member> findTop3HelloBy();


    /**
     * Named Query 와 JPA Repository 내부에 정의되는 쿼리문은 application loading 시점에 파싱을 한다.
     * (jpql -> sql 파싱. 어플리케이션 동작하는 동안 정적쿼리로 그대로 사용함.)
     * 그래서 중간에 오타가 나더라도 디버깅이 가능하다.
     *
     * 사용 빈도 : JPA Repository 내부에 쿼리 정의 >>>> NamedQuery
     */


    // jpql 이나 NamedQuery 사용 시, 반드시 @Param 으로 파라미터 특정해야함
    // 동명의 NamedQuery 존재 시 @Query 생략 가능
//    @Query(name="Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);

    // JPA Repository 인터페이스 내부에 @Query 를 사용해 바로 jpql 정의 가능
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    List<Member> findListByUsername(String username); // 컬렉션
    Member findMemberByUsername(String username); // 단건
    Optional<Member> findOptionalMemberByUsername(String username); // 단건

}
