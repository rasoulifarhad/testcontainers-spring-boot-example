package com.example;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;

import javax.annotation.PostConstruct;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(DemoApplicationTests.Configuration.class)
@WebIntegrationTest(randomPort = true)
public class DemoApplicationTests {

    @ClassRule
    public static GenericContainer redis = new GenericContainer("redis:alpine").withExposedPorts(6379);

    @org.springframework.context.annotation.Configuration
    @Import(DemoApplication.class)
    static class Configuration {

        @Autowired
        ConfigurableEnvironment environment;

        @PostConstruct
        public void init() {
            environment.getPropertySources().addFirst(new PropertySource("containers") {
                @Override
                public Object getProperty(String name) {
                    switch (name) {
                        case "spring.redis.host":
                            return redis.getContainerIpAddress();
                        case "spring.redis.port":
                            return redis.getMappedPort(6379);
                        default:
                            return null;
                    }
                }
            });
        }
    }

    @Value("${local.server.port}")
    int port;

    RestTemplate restTemplate = new RestTemplate();

    @Test
    public void simpleTest() {
        String fooResource = "http://localhost:" + port + "/foo";
        restTemplate.put(fooResource, "bar");

        assertEquals("bar", restTemplate.getForObject(fooResource, String.class));
    }

}
