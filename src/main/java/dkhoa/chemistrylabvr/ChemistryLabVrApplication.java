package dkhoa.chemistrylabvr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class ChemistryLabVrApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ChemistryLabVrApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ChemistryLabVrApplication.class, args);
    }

}