package study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;

import java.util.List;
import java.util.function.LongSupplier;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.*;

@Repository
public class MemberRepositoryImpl implements MemberRepositoryCustom {
//     extends QuerydslRepositorySupport
    /**
     * QuerydslRepositorySupport
     * entity manager 제공
     * 쿼리 시작은 from() 으로, select() 는 마지막에
     * <p>
     * 장점 : getQuerydsl().applyPagination() 으로 pageable -> limit, offset 으로 간편하게 변환 가능 (단 Sort 는 불가)
     * 한계 :
     * querydsl3 대상으로 만들었음. -> querydsl4에 나온 jpaQueryFactory 사용 불가능
     * QueryFactory 제공하지 않음
     * 스프링 데이터 `Sort` 기능이 정상 동작하지 않음 (오류 발생)
     */
//    public MemberRepositoryImpl() {
//        super(Member.class);
//    }

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition cond) {
//        return from(member)
//                .leftJoin(member.team, team)
//                .where(usernameEq(cond.getUsername()),
//                        teamNameEq(cond.getTeamName()),
//                        ageGoe(cond.getAgeGoe()),
//                        ageLoe(cond.getAgeLoe()))
//                .select(new QMemberTeamDto(
//                        member.id.as("memberId"),
//                        member.username,
//                        member.age,
//                        team.id.as("teamId"),
//                        team.name.as("teamName")))
//                .fetch();

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe()))
                .fetch();
    }

    /**
     * fetchResults() -> deprecated
     * 대신에 count 와 content 따로 쿼리하기
     * <p>
     * org.springframework.data.support.PageableExecutionUtils 사용
     * PageableExecutionUtils.getPage(content, pageable, LongSupplier(람다 표현 가능)) 으로 페이징
     * <p>
     * LongSupplier  로 넘겨준 카운트 쿼리는, 꼭 실행되어야만 할 때 실행됨.
     */
    @Override
    public Page<MemberTeamDto> searchPage(MemberSearchCondition cond, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(member.count())
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe()));

        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchOne());
    }

//    @Override
//    public Page<MemberTeamDto> searchPage2(MemberSearchCondition cond, Pageable pageable) {
//
//        JPQLQuery<MemberTeamDto> jpaQuery = from(member)
//                .leftJoin(member.team, team)
//                .where(usernameEq(cond.getUsername()),
//                        teamNameEq(cond.getTeamName()),
//                        ageGoe(cond.getAgeGoe()),
//                        ageLoe(cond.getAgeLoe()))
//                .select(new QMemberTeamDto(
//                        member.id.as("memberId"),
//                        member.username,
//                        member.age,
//                        team.id.as("teamId"),
//                        team.name.as("teamName")));
//
//        List<MemberTeamDto> fetch = getQuerydsl().applyPagination(pageable, jpaQuery).fetch();
//
//        JPAQuery<Long> countQuery = queryFactory
//                .select(member.count())
//                .from(member)
//                .leftJoin(member.team, team)
//                .where(usernameEq(cond.getUsername()),
//                        teamNameEq(cond.getTeamName()),
//                        ageGoe(cond.getAgeGoe()),
//                        ageLoe(cond.getAgeLoe()));
//
//        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchOne());
//    }


    private BooleanExpression usernameEq(String username) {
        return StringUtils.hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }


    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}
