package org.mangala.portfolio.portfolio.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mangala.portfolio.portfolio.adapter.repository.PortfolioRepository;
import org.mangala.portfolio.portfolio.domain.PortfolioEntity;
import org.mangala.portfolio.portfolio.usecase.impl.CreatePortfolioUseCaseImpl;
import org.mangala.portfolio.shared.exception.WalletLimitExceededException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePortfolioUseCaseTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @InjectMocks
    private CreatePortfolioUseCaseImpl useCase;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void execute_shouldCreatePortfolio_whenValidCommand() {
        // Given
        var command = new CreatePortfolioUseCase.CreatePortfolioCommand(
                userId, "My Portfolio", "Test description", null);

        when(portfolioRepository.save(any(PortfolioEntity.class))).thenAnswer(invocation -> {
            PortfolioEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        // When
        var response = useCase.execute(command);

        // Then
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("My Portfolio");
        assertThat(response.description()).isEqualTo("Test description");

        ArgumentCaptor<PortfolioEntity> captor = ArgumentCaptor.forClass(PortfolioEntity.class);
        verify(portfolioRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
    }

    @Test
    void execute_shouldCreatePortfolioWithWallets_whenWalletIdsProvided() {
        // Given
        List<UUID> walletIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        var command = new CreatePortfolioUseCase.CreatePortfolioCommand(
                userId, "My Portfolio", null, walletIds);

        when(portfolioRepository.save(any(PortfolioEntity.class))).thenAnswer(invocation -> {
            PortfolioEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        // When
        var response = useCase.execute(command);

        // Then
        assertThat(response.walletIds()).hasSize(2);
    }

    @Test
    void execute_shouldThrowException_whenWalletLimitExceeded() {
        // Given
        List<UUID> walletIds = List.of(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID() // 11 wallets
        );
        var command = new CreatePortfolioUseCase.CreatePortfolioCommand(
                userId, "My Portfolio", null, walletIds);

        // When/Then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(WalletLimitExceededException.class);

        verify(portfolioRepository, never()).save(any());
    }
}
