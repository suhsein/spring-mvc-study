package study.datajpa.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    // 상위 3개 결과, By 뒤에 조건 없으면 where 문 X
    List<Member> findTop3HelloBy();


    /**
     * Named Query 와 JPA Repository 내부에 정의되는 쿼리문은 application loading 시점에 파싱을 한다.
     * (jpql -> sql 파싱. 어플리케이션 동작하는 동안 정적쿼리로 그대로 사용함.)
     * 그래서 중간에 오타가 나더라도 디버깅이 가능하다.
     * <p>
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

    /**
     * Page 는 내부에 count 를 포함함
     * count 쿼리는 value 쿼리를 따라가므로 쓸모없는 join 을 한 상태에서 count 하게 될 수도 있음
     * 그런 경우에는 쿼리와 count 쿼리를 분리해서 명기
     */
    Page<Member> findByAge(int age, Pageable pageable);

    /**
     * Bulk Update Query
     * <p>
     * 보통 update 는 쿼리를 만들지 않고 setter 를 통해서 수정하면 Dirty checking 으로 반영되지만,
     * bulk update 쿼리의 경우 다음과 같이 쿼리를 만든다.
     *
     * @Modifying 어노테이션 필수
     * 포함하지 않으면, executeQuery 가 아닌 getResult 를 호출하게 된다.
     * <p>
     * 벌크성 쿼리의 단점 -> 영속성 컨텍스트의 일관성 유지 불가
     * 해결방법 => clearAutomatically (영속성 컨텍스트 초기화)
     */
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    // @EntityGraph
    @EntityGraph("Member.all")
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(@Param("username") String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(@Param("username") String username);
}
