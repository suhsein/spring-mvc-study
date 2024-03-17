package com.practicebbs.springbbs.controller;

import com.practicebbs.springbbs.domain.Member;
import com.practicebbs.springbbs.service.MemberService;
import com.practicebbs.springbbs.service.ScriptUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
public class MemberController {
    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping(value="/join")
    public String joinPage(){
        return "join";
    }

    @PostMapping("/join")
    public String create(MemberForm form){
        Member member = new Member();
        member.setId(form.getId());
        member.setPw(form.getPw());
        member.setGender(form.getGender());
        member.setName(form.getName());
        member.setEmail(form.getEmail());
        memberService.join(member);
        return "redirect:/";
    }

    @GetMapping(value="/login")
    public String loginPage(){
        return "login";
    }

    @PostMapping(value="/login")
    public String loginForm(HttpServletRequest request, HttpServletResponse response) {

        return "main";
    }
}
