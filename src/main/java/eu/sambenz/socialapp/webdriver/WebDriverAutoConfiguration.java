package eu.sambenz.socialapp.webdriver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import static org.openqa.selenium.chrome.ChromeDriverService.createDefaultService;

@Configuration
@ConditionalOnClass(WebDriver.class)
@EnableConfigurationProperties(WebDriverConfigurationProperties.class)
@Import({ChromeDriverFactory.class, FirefoxDriverFactory.class, SafariDriverFactory.class})
public class WebDriverAutoConfiguration {
    @Primary
    @Bean(destroyMethod = "quit")
    @ConditionalOnMissingBean(WebDriver.class)
    public WebDriver webDriver(FirefoxDriverFactory firefoxDriverFactory, SafariDriverFactory safariDriverFactory, ChromeDriverFactory chromeDriverFactory) {
        WebDriver driver = firefoxDriverFactory.getObject();

        if(driver == null) {
            driver = safariDriverFactory.getObject();
        }

        if(driver == null) {
            driver = chromeDriverFactory.getObject();
        }

        if(driver == null) {
            driver = new HtmlUnitDriver();
        }

        return driver;
    }

    @Bean(destroyMethod = "stop")
    @Lazy
    public ChromeDriverService chromeDriverService() {
        return createDefaultService();
    }
}
