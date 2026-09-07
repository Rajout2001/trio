package fr.univ.orleans.trioapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:trio-context-db;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    }
)
class TrioApiApplicationTests {

    @Test
    void contextLoads() {
    }
}
