package hello;

import hello.boot.MySpringApplication;
import hello.boot.MySpringBootApplication;
import hello.spring.HelloConfig;

import java.io.IOException;

@MySpringBootApplication
public class MySpringBootMain {
    public static void main(String[] args) throws IOException {
        System.out.println("MySpringBootMain.main");
        MySpringApplication.run(MySpringBootMain.class, args);
    }
}
