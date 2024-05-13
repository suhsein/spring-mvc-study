package study.querydsl.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.repository.support.Querydsl4RepositorySupport;

import java.util.List;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

/**
 * Querydsl 지원 클래스를 직접 생성함.
 *
 * 기존의 QuerydslRepositorySupport
 *   => 페이지네이션 getQuerydsl().applyPagination() 으로 pageable -> limit, offset 으로 간편하게 변환 가능
 *   => 하지만 Sort 불가능
 *
 * 지원 클래스를 직접 구현하여 Sort 가 가능하도록 할 수 있다.
 * pageable 에 sort 조건을 함께 포함해서 파라미터로 넘기면 sort 적용됨.
 * ex) PageRequest pageable = PageRequest.of(0, 10, Sort.by("age"));
 */
@Repository
public class MemberTestRepository extends Querydsl4RepositorySupport {
    public MemberTestRepository() {
        super(Member.class);
    }

    public List<Member> basicSelect() {
        return select(member)
                .from(member)
                .fetch();
    }

    public List<Member> basicSelectFrom() {
        return selectFrom(member)
                .fetch();
    }

    public Page<Member> searchPageByApplyPage(MemberSearchCondition cond,
                                              Pageable pageable) {
        JPAQuery<Member> contentQuery = selectFrom(member)
                .leftJoin(member.team, team)
                .where(usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe()));

        JPAQuery<Long> countQuery = select(member.count())
                .from(member);

        List<Member> content = getQuerydsl().applyPagination(pageable, contentQuery)
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchOne());
    }

    public Page<Member> applyPagination(MemberSearchCondition cond,
                                        Pageable pageable) {
        return applyPagination(pageable, query -> query
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe())
                )
        );
    }

    public Page<Member> applyPagination2(MemberSearchCondition cond,
                                         Pageable pageable) {
        return applyPagination(pageable,
                contentQuery -> contentQuery.selectFrom(member)
                        .leftJoin(member.team, team)
                        .where(usernameEq(cond.getUsername()),
                                teamNameEq(cond.getTeamName()),
                                ageGoe(cond.getAgeGoe()),
                                ageLoe(cond.getAgeLoe())
                        ),
                countQuery -> countQuery.select(member.count())
                        .from(member)
        );
    }

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
