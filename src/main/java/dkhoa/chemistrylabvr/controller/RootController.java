package dkhoa.chemistrylabvr.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public String healthCheck() {
        return "redirect:/swagger-ui/index.html";
    }
}
