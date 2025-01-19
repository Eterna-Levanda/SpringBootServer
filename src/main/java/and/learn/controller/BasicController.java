package and.learn.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class BasicController {

    @GetMapping("/")
    public String get() {
        return "funziona!";
    }
}
