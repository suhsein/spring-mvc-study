package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory; // 필드로 빼서 사용 가능. 멀티 스레드 환경에서 문제 없도록 설계되어서, 동시성 문제 고민 안해도 됨

    @BeforeEach
    void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    void startJPQL() {
        // member1을 찾아라.
        String qlString = "select m from Member m" +
                " where m.username = :username";
        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /**
     * QClass 객체는 별칭을 줘서 새 객체 생성하거나, 기본 인스턴스로도 사용 가능
     * 기본 인스턴스를 static import 로 사용하는 것이 권장됨
     * <p>
     * 같은 테이블을 join 해서 사용하는 경우 alias 필요하므로 이럴 때는 alias 줘서 새 객체 생성.
     * alias 는 jpql 에서 그대로 사용된다.
     */
    @Test
    void startQuerydsl() {
        Member findMember = queryFactory.select(member)
                .from(member)
                .where(member.username.eq("member1")) // 파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void search() {
        Member findMember = queryFactory.selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /**
     * where 에서 and 조건 여러개 붙을 때 and 로 조합해도 되고, comma 로 구분해서 조합해도 된다.
     */
    @Test
    void searchAndParam() {
        Member findMember = queryFactory.selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void resultFetch() {
//        List<Member> fetch = queryFactory
//                .selectFrom(member)
//                .fetch();
//
//        Member fetchOne = queryFactory
//                .selectFrom(member)
//                .fetchOne();
//
//        Member fetchFirst = queryFactory
//                .selectFrom(member)
//                .fetchFirst();// limit(1).fetchOne() 과 같음
//
//        QueryResults<Member> results = queryFactory
//                .selectFrom(member)
//                .fetchResults();

        long total = queryFactory
                .selectFrom(member)
                .fetchCount();

    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * <p>
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(
                        member.age.desc(),
                        member.username.asc().nullsLast()
                )
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void paging2() {
        QueryResults<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();
        assertThat(result.getTotal()).isEqualTo(4);
        assertThat(result.getLimit()).isEqualTo(2);
        assertThat(result.getOffset()).isEqualTo(1);
        assertThat(result.getResults().size()).isEqualTo(2);
    }

    /**
     * aggregation 결과는 tuple 로 나옴 (여러개이면 튜플 리스트)
     * SQL 에서와 같은 기능으로 groupBy, having 사용 가능.
     */
    @Test
    void aggregation() {
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    public void group() throws Exception {
        List<Tuple> result = queryFactory
                .select(
                        team.name,
                        member.age.avg()
                )
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    /**
     * 팀 A 에 소속된 모든 회원
     */
    @Test
    void join() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인
     * 카르테시안 곱 통해 연관관계 없는 필드로 조인. QueryDSL 에서도 세타 조인 가능.
     * 단, 이 경우에는 외부 조인은 안됨
     * <p>
     * 회원의 이름이 팀 이름과 같은 회원을 조회
     */
    @Test
    void theta_join() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /**
     * on 절 사용 => !outer join! 시
     * 1. 조인 대상 필터링
     * 2. 세타 조인 (연관관계가 없는 경우)
     *
     * inner join 에서 on 절 사용해도 문제는 없지만,
     * inner join 에서는 익숙한 where 쓰고, outer join 시에만 on 절 쓰기
     */

    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA 인 팀만 조인, 회원은 모두 조회
     * JPQL : select m, t from Member m left join m.team t on t.name = 'teamA'
     */
    @Test
    public void join_on_filtering() throws Exception {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }


    /**
     * !연관관계가 없는! 엔티티 외부 조인
     * 회원의 이름이 팀 이름과 같은 대상 외부 조인
     */
    @Test
    public void join_on_no_relation() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo() throws Exception {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }

    /**
     * 페치 조인 사용 시 그냥 조인과 문법 같지만, 뒤에 fetchJoin() 붙여주면 됨.
     */
    @Test
    public void fetchJoinUse() throws Exception {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 적용").isTrue();
    }

    /**
     * subQuery 사용 -> subQuery 작성 시 alias 써야함
     * subQuery 작성은 JPAExpressions 로 할 수 있다. JPAExpressions 는 static import 가능
     *
     * JPA 표준 스펙에서 select 절 subQuery 안되지만, 하이버네이트에서 지원해줘서 사용 가능
     *
     * 하지만 from 절에서는 무조건 subQuery 안됨.
     * -> 해결방법 : 1. 서브쿼리를 join 으로 바꾸기 / 2. 쿼리 두 개로 분리해서 실행 / 3. nativeSQL 사용
     */


    /**
     * 나이가 가장 많은 회원을 조회
     */
    @Test
    public void subQuery() throws Exception {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    /**
     * 나이가 평균 이상인 회원을 조회
     */
    @Test
    public void subQueryGoe() throws Exception {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(30, 40);
    }

    /**
     * 나이가 평균 이상인 회원을 조회
     */
    @Test
    public void subQueryIn() throws Exception {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(20,30, 40);
    }

    @Test
    public void selectSubQuery() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Tuple> result = queryFactory
                .select(member.username,
                        select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * Case 문
     * 간단한 case when, otherwise 로 짤 수 있음
     * 복잡한 case 짤 때는 CaseBuilder 사용
     *
     * 하지만, DB 에서 보여주는 데이터를 바꾸어서 보여주는 것은 권장하지 않는다.
     * DB 는 데이터를 가져오는 용도로 사용하고 전환하여 보여주는 것은 프레젠테이션 단에서 하는 것이 좋음
     *
     * case 를 사용하여 효율이 좋아지는 경우에만 사용하기
     */
    @Test
    public void basicCase() throws Exception {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타등등"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void complexCase() throws Exception {
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    /**
     * constant 는 Expressions 사용해 설정 가능
     */

    @Test
    public void constant() throws Exception {
        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * concat 사용 시 문자열이 아닌 다른 타입은 문자열로 변환이 필요함.
     * .stringValue()를 사용해 문자열로 변환 후 concat 가능
     * 특히 enum 사용 시 좋음
     */
    @Test
    public void concat() throws Exception {
        // {username}_{age}
        List<String> result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }


    @Test
    public void simpleProjection() throws Exception {
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    /**
     * 프로젝션 대상이 여러개 -> 튜플 반환
     * 튜플에서 값을 꺼낼 때는 get(member.username) 과 같이 꺼내면 됨.
     *
     * 주의 : 튜플의 경우 QueryDSL 에서 사용하는 타입이기 때문에,
     * 리포지토리 계층을 넘어서서 서비스 계층에서 사용하는 것은 좋지 않음
     *
     * 이유는 서비스가 리포지토리 구현체에 의존적이면 안되기 때문이다. 구현체를 바꿀 때 문제가 됨.
     * 해결방법 => 서비스 계층에 나갈 때는 DTO 로 변환시켜서 내보내기
     */
    @Test
    public void tupleProjection() throws Exception {
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void findDtoByJPQL() throws Exception {
        List<MemberDto> result = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age)" +
                        " from Member m", MemberDto.class)
                .getResultList();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * DTO 조회
     * 주의 : DTO 에 반드시 NoArgsConstructor 존재해야 함.
     */

    /**
     * setter 주입 -> Projections.bean() 으로 가능함
     * DTO 에 setter 있어야 함.
     */
    @Test
    public void findDtoBySetter() throws Exception {
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * field 주입 -> Projections.fields() 로 가능함.
     * 필드 private 이어도 알아서 처리해줌.
     */

    @Test
    public void findDtoByField() throws Exception {
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * DTO 와 엔티티 간 필드명이 다른 경우 -> matching 되지 않음
     * => as 써서 alias 를 주고 매칭 시켜주면 됨.
     *
     * subQuery 의 필드에 적용할 때는 ExpressionUtils.as()를 사용하면 됨.
     * 첫번째 파라미터로 subQuery, 두번째 파라미터로 alias
     */
    @Test
    public void findUserDtoByField() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "age")
                ))
                .from(member)
                .fetch();
        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    /**
     * 생성자 주입 -> Projections.constructor() 로 가능함.
     * 생성자 주입의 경우 필드명이 아니라 타입을 보고 주입하게 됨. -> 필드명이 달라도 as 안 써도 됨
     */

    @Test
    public void findDtoByConstructor() throws Exception {
        List<UserDto> result = queryFactory
                .select(Projections.constructor(UserDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    /**
     * DTO 생성자에 @QueryProjection 붙여서 쿼리 프로젝션 가능
     * DTO 도 QClass 로 생성됨. => new 키워드를 쓸 수 있음
     * constructor 방식과 비슷함
     *
     * * 장점 : constructor 방식에서는 컴파일할 때 오류를 잡지 못함.(런타임 오류 발생)
     *   하지만 QueryProjection 은 컴파일 오류를 잡을 수 있음.
     *   또한 생성자가 호출되는 것까지 보장해줌
     *
     * * 단점 : 아키텍쳐적 문제. DTO 자체가 queryDSL 에 의존적으로 설계됨.
     *    만약 queryDSL 라이브러리를 빼면 오류 발생
     */
    @Test
    public void findDtoByQueryProjection() throws Exception {
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * 동적 쿼리
     */

    @Test
    public void dynamicQuery_BooleanBuilder() throws Exception {
        String usernameParam = "member1";
        Integer ageParam = null;

        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    /**
     * BooleanBuilder 를 사용하여 조립하는 방법
     */
    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if(StringUtils.hasText(usernameCond)){
            builder.and(member.username.eq(usernameCond));
        }
        if(ageCond != null){
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }


    @Test
    public void dynamicQuery_WhereParam() throws Exception {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    /**
     * Where 다중 파라미터 사용. 권장되는 방법
     * where 에 들어가는 predicate 메서드 중 null 인 predicate 은 무시됨.
     *
     * 장점 : 만들어 둔 predicate 메서드들을 여러 쿼리에 재사용, 조립이 가능하다.
     * => QueryDSL 이 java 코드를 사용함으로써 얻을 수 있는 장점을 극대화.
     */
    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                .where(allEq(usernameCond, ageCond))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return StringUtils.hasText(usernameCond) ? member.username.eq(usernameCond): null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    // 조립이 가능함. 이 때는 반환 모두 BooleanExpression 으로 해줘야 함.
    private BooleanExpression allEq(String usernameCond, Integer ageCond){
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }

    /**
     * bulk update
     * update, set 으로 업데이트를 할 수 있고, 쿼리 실행 시 execute 사용. 영향을 받은 row 수가 반환됨
     *
     * 주의 : jpql 에서 bulk update 를 했던 것과 같이, bulk update 는 영속성 컨텍스트의 값을 바꾸지 않고 바로 DB 에 쿼리를 함.
     * -> 영속성 컨텍스트의 값은 update 전이다.(불일치) 그러므로 bulk update 를 한 후에 다른 로직 실행하면 문제됨
     * 
     * 이유 : repeatable read - DB 에서 값을 읽더라도 이미 영속성 컨텍스트에 값이 있다면 영속성 컨텍스트가 우선 됨.
     * 
     * 해결방법 : bulk update 이후 다른 로직을 실행해야 한다면, 영속성 컨텍스트를 초기화 한다.
     * 그럼 다시 DB 로부터 값을 읽어서 영속성 컨텍스트에 채울 수 있다.
     */

    @Test
    public void bulkUpdate() throws Exception {
        // member1 = 10 -> 비회원
        // member2 = 20 -> 비회원
        // member3 = 30 -> 유지
        // member4 = 40 -> 유지


        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();

        em.flush();
        em.clear();

        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();

        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }

    /**
     * bulk update
     * 사칙연산 : add, subtract, multiply, divide
     */
    @Test
    public void bulkAdd() throws Exception {
        long count = queryFactory
                .update(member)
                .set(member.age, member.age.add(2))
                .execute();

    }

    @Test
    public void bulkdDelete() throws Exception {
        long execute = queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();

    }

    /**
     * sql function 사용 -> Expressions.stringTemplate 으로
     * function('메서드명', {파라미터인덱스}, ...), 파라미터
     *
     * (물론 sql function 사용 시에도 벌크 수정을 하게 되면 영속성 컨텍스트 초기화 필요)
     */
    @Test
    public void sqlFunction() throws Exception {
        List<String> result = queryFactory
                .select(Expressions.stringTemplate(
                        "function('replace', {0}, {1}, {2})",
                        member.username, "member", "M"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }

        em.flush();
        em.clear();

        List<Member> members = queryFactory
                .selectFrom(member)
                .fetch();

        for (Member member1 : members) {
            System.out.println("member1 = " + member1);
        }
    }

    /**
     * ANSI SQL 에 있는 메서드들은 Expressions.stringTemplate 을 사용하지 않고도 사용 가능함.
     */
    @Test
    public void sqlFunction2() throws Exception {
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
//                .where(member.username.eq(
//                        Expressions.stringTemplate("function('lower', {0})", member.username)))
                .where(member.username.eq(member.username.lower()))
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }
}
