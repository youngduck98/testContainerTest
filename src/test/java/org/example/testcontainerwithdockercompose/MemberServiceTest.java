package org.example.testcontainerwithdockercompose;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;

@SpringBootTest
@Testcontainers
class MemberServiceTest {

    public static final String MYSQL_DB = "mysqldb";
    public static final int MY_SQL_PORT = 3306;

    @Container
    public static final DockerComposeContainer dockerComposeContainer =
            new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"))
                    .withExposedService(
                            MYSQL_DB,
                            MY_SQL_PORT,
                            Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30))
                    );

    @DynamicPropertySource
    public static void overrideProps(DynamicPropertyRegistry dynamicPropertyRegistry){

        final String host=dockerComposeContainer.getServiceHost(MYSQL_DB,MY_SQL_PORT);
        final Integer port=dockerComposeContainer.getServicePort(MYSQL_DB,MY_SQL_PORT);
        dynamicPropertyRegistry.add("spring.datasource.url",
                ()->"jdbc:mysql://%s:%d/test_container_test".formatted(host,port));
        dynamicPropertyRegistry.add("spring.datasource.username",()->"root");
        dynamicPropertyRegistry.add("spring.datasource.password",()->"password");
        dynamicPropertyRegistry.add("spring.jpa.hibernate.ddl-auto",()->"create");
    }

    @Autowired
    private MemberService memberService;

    @Test
    void 회원_저장() throws Exception {

        Member member = memberService.save(new Member(1L, "name"));

        Assertions.assertThat(member.getId()).isEqualTo(1);
        Assertions.assertThat(member.getName()).isEqualTo("name");
    }

}