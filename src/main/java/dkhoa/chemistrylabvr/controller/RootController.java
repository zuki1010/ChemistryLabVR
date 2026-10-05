package dkhoa.chemistrylabvr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class RootController {

    @GetMapping("/")
    public String healthCheck() {
        return "redirect:/swagger-ui/index.html";
    }
}
