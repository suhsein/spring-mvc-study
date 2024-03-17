package hello.hellospring.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class MemberForm {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
