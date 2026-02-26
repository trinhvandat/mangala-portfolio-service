package org.mangala.portfolio.portfolio.adapter.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mangala.portfolio.TestContainersConfig;
import org.mangala.portfolio.portfolio.domain.PortfolioEntity;
import org.mangala.portfolio.portfolio.domain.PortfolioWalletEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
class PortfolioRepositoryIntegrationTest {

    @Autowired
    private PortfolioRepository portfolioRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        portfolioRepository.deleteAll();
    }

    @Test
    void findByUserId_shouldReturnUserPortfolios() {
        // Given
        PortfolioEntity portfolio1 = createPortfolio("Portfolio 1");
        PortfolioEntity portfolio2 = createPortfolio("Portfolio 2");
        portfolioRepository.saveAll(List.of(portfolio1, portfolio2));

        // When
        List<PortfolioEntity> result = portfolioRepository.findByUserId(userId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(PortfolioEntity::getName)
                .containsExactlyInAnyOrder("Portfolio 1", "Portfolio 2");
    }

    @Test
    void findByUserId_shouldExcludeDeletedPortfolios() {
        // Given
        PortfolioEntity active = createPortfolio("Active Portfolio");
        PortfolioEntity deleted = createPortfolio("Deleted Portfolio");
        deleted.setDeletedAt(Instant.now());
        portfolioRepository.saveAll(List.of(active, deleted));

        // When
        List<PortfolioEntity> result = portfolioRepository.findByUserId(userId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Active Portfolio");
    }

    @Test
    void findByIdAndNotDeleted_shouldReturnPortfolio_whenNotDeleted() {
        // Given
        PortfolioEntity portfolio = createPortfolio("Test Portfolio");
        portfolio = portfolioRepository.save(portfolio);

        // When
        Optional<PortfolioEntity> result = portfolioRepository.findByIdAndNotDeleted(portfolio.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Portfolio");
    }

    @Test
    void findByIdAndNotDeleted_shouldReturnEmpty_whenDeleted() {
        // Given
        PortfolioEntity portfolio = createPortfolio("Deleted Portfolio");
        portfolio.setDeletedAt(Instant.now());
        portfolio = portfolioRepository.save(portfolio);

        // When
        Optional<PortfolioEntity> result = portfolioRepository.findByIdAndNotDeleted(portfolio.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByIdWithWallets_shouldReturnPortfolioWithWallets() {
        // Given
        PortfolioEntity portfolio = createPortfolio("Portfolio with Wallets");
        UUID walletId1 = UUID.randomUUID();
        UUID walletId2 = UUID.randomUUID();

        portfolio = portfolioRepository.save(portfolio);

        PortfolioWalletEntity wallet1 = PortfolioWalletEntity.builder()
                .portfolioId(portfolio.getId())
                .walletId(walletId1)
                .build();
        wallet1.setPortfolio(portfolio);

        PortfolioWalletEntity wallet2 = PortfolioWalletEntity.builder()
                .portfolioId(portfolio.getId())
                .walletId(walletId2)
                .build();
        wallet2.setPortfolio(portfolio);

        portfolio.getWallets().add(wallet1);
        portfolio.getWallets().add(wallet2);
        portfolioRepository.save(portfolio);

        // When
        Optional<PortfolioEntity> result = portfolioRepository.findByIdWithWallets(portfolio.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getWallets()).hasSize(2);
    }

    @Test
    void countByUserId_shouldReturnCorrectCount() {
        // Given
        portfolioRepository.saveAll(List.of(
                createPortfolio("Portfolio 1"),
                createPortfolio("Portfolio 2"),
                createPortfolio("Portfolio 3")
        ));

        // When
        long count = portfolioRepository.countByUserId(userId);

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    void findDefaultByUserId_shouldReturnDefaultPortfolio() {
        // Given
        PortfolioEntity regular = createPortfolio("Regular Portfolio");
        PortfolioEntity defaultPortfolio = createPortfolio("Default Portfolio");
        defaultPortfolio.setIsDefault(true);
        portfolioRepository.saveAll(List.of(regular, defaultPortfolio));

        // When
        Optional<PortfolioEntity> result = portfolioRepository.findDefaultByUserId(userId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Default Portfolio");
        assertThat(result.get().getIsDefault()).isTrue();
    }

    private PortfolioEntity createPortfolio(String name) {
        return PortfolioEntity.builder()
                .userId(userId)
                .name(name)
                .description("Test description")
                .isDefault(false)
                .build();
    }
}
