package org.mangala.portfolio.portfolio.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mangala.portfolio.TestContainersConfig;
import org.mangala.portfolio.portfolio.adapter.repository.PortfolioRepository;
import org.mangala.portfolio.portfolio.adapter.web.dto.CreatePortfolioRequestDTO;
import org.mangala.portfolio.portfolio.adapter.web.dto.UpdatePortfolioRequestDTO;
import org.mangala.portfolio.portfolio.domain.PortfolioEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
class PortfolioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PortfolioRepository portfolioRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        portfolioRepository.deleteAll();
        userId = UUID.randomUUID();
    }

    @Test
    void createPortfolio_shouldReturnCreatedPortfolio() throws Exception {
        CreatePortfolioRequestDTO request = new CreatePortfolioRequestDTO();
        request.setName("My Portfolio");
        request.setDescription("Test description");

        mockMvc.perform(post("/api/v1/portfolios")
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("My Portfolio"))
                .andExpect(jsonPath("$.description").value("Test description"));
    }

    @Test
    void createPortfolio_shouldReturnBadRequest_whenNameMissing() throws Exception {
        CreatePortfolioRequestDTO request = new CreatePortfolioRequestDTO();
        request.setDescription("Test description");

        mockMvc.perform(post("/api/v1/portfolios")
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listPortfolios_shouldReturnUserPortfolios() throws Exception {
        // Create portfolios
        createTestPortfolio("Portfolio 1");
        createTestPortfolio("Portfolio 2");

        mockMvc.perform(get("/api/v1/portfolios")
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Portfolio 1", "Portfolio 2")));
    }

    @Test
    void getPortfolio_shouldReturnPortfolio_whenUserOwns() throws Exception {
        PortfolioEntity portfolio = createTestPortfolio("Test Portfolio");

        mockMvc.perform(get("/api/v1/portfolios/{id}", portfolio.getId())
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(portfolio.getId().toString()))
                .andExpect(jsonPath("$.name").value("Test Portfolio"));
    }

    @Test
    void getPortfolio_shouldReturnForbidden_whenUserDoesNotOwn() throws Exception {
        UUID otherUserId = UUID.randomUUID();
        PortfolioEntity portfolio = portfolioRepository.save(PortfolioEntity.builder()
                .userId(otherUserId)
                .name("Other User Portfolio")
                .build());

        mockMvc.perform(get("/api/v1/portfolios/{id}", portfolio.getId())
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPortfolio_shouldReturnNotFound_whenPortfolioDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/portfolios/{id}", UUID.randomUUID())
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePortfolio_shouldUpdatePortfolio() throws Exception {
        PortfolioEntity portfolio = createTestPortfolio("Original Name");

        UpdatePortfolioRequestDTO request = new UpdatePortfolioRequestDTO();
        request.setName("Updated Name");
        request.setDescription("Updated description");

        mockMvc.perform(put("/api/v1/portfolios/{id}", portfolio.getId())
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void deletePortfolio_shouldSoftDelete() throws Exception {
        PortfolioEntity portfolio = createTestPortfolio("To Delete");

        mockMvc.perform(delete("/api/v1/portfolios/{id}", portfolio.getId())
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))))
                .andExpect(status().isNoContent());

        // Verify soft delete
        mockMvc.perform(get("/api/v1/portfolios/{id}", portfolio.getId())
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPortfolio_shouldReturnUnauthorized_whenNoToken() throws Exception {
        CreatePortfolioRequestDTO request = new CreatePortfolioRequestDTO();
        request.setName("My Portfolio");

        mockMvc.perform(post("/api/v1/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    private PortfolioEntity createTestPortfolio(String name) {
        return portfolioRepository.save(PortfolioEntity.builder()
                .userId(userId)
                .name(name)
                .description("Test description")
                .build());
    }
}
