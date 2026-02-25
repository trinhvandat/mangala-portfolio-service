package org.mangala.portfolio.portfolio.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.mangala.portfolio.portfolio.adapter.repository.PortfolioRepository;
import org.mangala.portfolio.portfolio.usecase.ListPortfoliosUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListPortfoliosUseCaseImpl implements ListPortfoliosUseCase {

    private final PortfolioRepository portfolioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioSummary> execute(UUID userId) {
        return portfolioRepository.findByUserId(userId).stream()
                .map(p -> new PortfolioSummary(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getIsDefault(),
                        p.getWallets().size()
                ))
                .collect(Collectors.toList());
    }
}
