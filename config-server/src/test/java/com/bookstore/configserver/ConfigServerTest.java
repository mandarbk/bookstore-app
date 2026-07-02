package com.bookstore.configserver;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // 1. Run server on random port
public class ConfigServerTest {

    @TempDir
    static Path tempGitDir;

    @Autowired
    private ApplicationContext applicationContext;

    @LocalServerPort
    private int port;

    private RestTestClient restTestClient;

    @Test
    void contextLoads() {
    }

    @BeforeAll
    public static void initGitRepo() throws IOException, GitAPIException {
        // 1. Create a temp directory
        tempGitDir = Files.createTempDirectory("config-repo");
        // 2. Initialize the Git repo
        try (Git git = Git.init().setDirectory(tempGitDir.toFile()).call()) {
            // IMPORTANT: Create at least one file and commit it.
            // A new, empty git repo is often considered "invalid" by JGit/Config Server.
            // Create a dummy configuration file (e.g., for 'testapp' in 'default' profile)
            Path configFile = tempGitDir.resolve("testapp.properties");
            Files.writeString(configFile, "user.welcome.message=Hello from Dummy Git Backend!");

            // 5. Commit the file to the repository
            git.add().addFilepattern("testapp.properties").call();
            git.commit().setMessage("Initial dummy config commit").call();

        }
    }

    @DynamicPropertySource
    static void registerGitProperties(DynamicPropertyRegistry registry) {
        // 6. Dynamically override the server's Git URI to point to our local temp repo
        String gitUri = tempGitDir.toUri().toString();
        registry.add("spring.cloud.config.server.git.uri", () -> gitUri);
        // Fix: Disable remote pulling so JGit doesn't attempt an upstream merge
        registry.add("spring.cloud.config.server.git.clone-on-start", () -> "false");
        registry.add("spring.cloud.config.server.git.force-pull", () -> "false");
    }

    @Autowired
    void setApplicationContext(ApplicationContext context) {
        // Boot 4 approach: Bind the unified RestTestClient to the active server context
        this.restTestClient = RestTestClient.bindToApplicationContext((WebApplicationContext) context)
                .build();
    }

    @Test
    void shouldFetchConfigurationFromDummyGitBackend() {
        // 7. Hit the config server endpoint format: /{application}/{profile}
        String url = "http://localhost:" + port + "/testapp/default";
        restTestClient.get()
                    .uri(url)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(String.class)
                    .value(body -> {
                        assertThat(body).isNotNull();
                        assertThat(body).contains("Hello from Dummy Git Backend");
                    });
    }
}
