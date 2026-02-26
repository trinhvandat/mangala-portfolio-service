package org.mangala.portfolio.portfolio.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mangala.portfolio.portfolio.adapter.repository.PortfolioRepository;
import org.mangala.portfolio.portfolio.domain.PortfolioEntity;
import org.mangala.portfolio.portfolio.usecase.impl.DeletePortfolioUseCaseImpl;
import org.mangala.portfolio.shared.exception.PortfolioAccessDeniedException;
import org.mangala.portfolio.shared.exception.PortfolioNotFoundException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeletePortfolioUseCaseTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @InjectMocks
    private DeletePortfolioUseCaseImpl useCase;

    private UUID userId;
    private UUID portfolioId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        portfolioId = UUID.randomUUID();
    }

    @Test
    void execute_shouldSoftDeletePortfolio_whenUserOwnsPortfolio() {
        // Given
        PortfolioEntity portfolio = PortfolioEntity.builder()
                .id(portfolioId)
                .userId(userId)
                .name("Test Portfolio")
                .build();

        when(portfolioRepository.findByIdAndNotDeleted(portfolioId)).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.save(any())).thenReturn(portfolio);

        var command = new DeletePortfolioUseCase.DeletePortfolioCommand(portfolioId, userId);

        // When
        useCase.execute(command);

        // Then
        ArgumentCaptor<PortfolioEntity> captor = ArgumentCaptor.forClass(PortfolioEntity.class);
        verify(portfolioRepository).save(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    @Test
    void execute_shouldThrowNotFoundException_whenPortfolioNotFound() {
        // Given
        when(portfolioRepository.findByIdAndNotDeleted(portfolioId)).thenReturn(Optional.empty());

        var command = new DeletePortfolioUseCase.DeletePortfolioCommand(portfolioId, userId);

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
                .build();

        when(portfolioRepository.findByIdAndNotDeleted(portfolioId)).thenReturn(Optional.of(portfolio));

        var command = new DeletePortfolioUseCase.DeletePortfolioCommand(portfolioId, userId);

        // When/Then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(PortfolioAccessDeniedException.class);
        verify(portfolioRepository, never()).save(any());
    }
}
