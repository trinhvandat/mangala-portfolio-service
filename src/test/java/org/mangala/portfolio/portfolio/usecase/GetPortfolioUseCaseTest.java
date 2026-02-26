package org.mangala.portfolio.portfolio.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mangala.portfolio.portfolio.adapter.repository.PortfolioRepository;
import org.mangala.portfolio.portfolio.domain.PortfolioEntity;
import org.mangala.portfolio.portfolio.usecase.impl.GetPortfolioUseCaseImpl;
import org.mangala.portfolio.shared.exception.PortfolioAccessDeniedException;
import org.mangala.portfolio.shared.exception.PortfolioNotFoundException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPortfolioUseCaseTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @InjectMocks
    private GetPortfolioUseCaseImpl useCase;

    private UUID userId;
    private UUID portfolioId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        portfolioId = UUID.randomUUID();
    }

    @Test
    void execute_shouldReturnPortfolio_whenUserOwnsPortfolio() {
        // Given
        PortfolioEntity portfolio = PortfolioEntity.builder()
                .id(portfolioId)
                .userId(userId)
                .name("Test Portfolio")
                .description("Test Description")
                .isDefault(false)
                .wallets(new HashSet<>())
                .build();

        when(portfolioRepository.findByIdWithWallets(portfolioId)).thenReturn(Optional.of(portfolio));

        var command = new GetPortfolioUseCase.GetPortfolioCommand(portfolioId, userId);

        // When
        var response = useCase.execute(command);

        // Then
        assertThat(response.id()).isEqualTo(portfolioId);
        assertThat(response.name()).isEqualTo("Test Portfolio");
        assertThat(response.description()).isEqualTo("Test Description");
    }

    @Test
    void execute_shouldThrowNotFoundException_whenPortfolioNotFound() {
        // Given
        when(portfolioRepository.findByIdWithWallets(portfolioId)).thenReturn(Optional.empty());

        var command = new GetPortfolioUseCase.GetPortfolioCommand(portfolioId, userId);

        // When/Then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(PortfolioNotFoundException.class);
    }

    @Test
    void execute_shouldThrowAccessDenied_whenUserDoesNotOwnPortfolio() {
        // Given
        UUID otherUserId = UUID.randomUUID();
        PortfolioEntity portfolio = PortfolioEntity.builder()
                .id(portfolioId)
                .userId(otherUserId)
                .name("Test Portfolio")
                .wallets(new HashSet<>())
                .build();

        when(portfolioRepository.findByIdWithWallets(portfolioId)).thenReturn(Optional.of(portfolio));

        var command = new GetPortfolioUseCase.GetPortfolioCommand(portfolioId, userId);

        // When/Then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(PortfolioAccessDeniedException.class);
    }
}
