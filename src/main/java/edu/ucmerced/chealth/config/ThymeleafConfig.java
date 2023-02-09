package edu.ucmerced.chealth.config;

import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/*
 * Configures the Thymeleaf layout dialect. This is probably a temporary measure. It is
 * necessary as of thymeleaf-layout-dialect 3.0, but it's expected that Spring Boot's
 * Thymeleaf support will catch up and make it unnecessary. Therefore, I recommend
 * periodically verifying whether it's needed.
 *
 * To check: Remove the bean (comment out the entire method below) and run the application,
 * and load the home page. If it's rendered properly, this can be removed.
 *
 * See also: https://ultraq.github.io/thymeleaf-layout-dialect/migrating-to-3.0/
 *
 * "As a consequence of this move, if you use Spring Boot to automatically configure the layout
 * dialect, you will now need to specify it yourself until Spring Boot is updated to support
 * version 3 of the layout dialect."
 */

@Component
public class ThymeleafConfig {
    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }
}
