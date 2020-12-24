package ch.so.agi.standortkarte;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

@ServletComponentScan
@SpringBootApplication
@Configuration
public class BootGwtApplication {
	public static void main(String[] args) {
		SpringApplication.run(BootGwtApplication.class, args);
	}
	
    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}
