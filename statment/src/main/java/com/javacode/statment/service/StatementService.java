package com.javacode.statment.service;

import com.javacode.statment.dto.LoanOfferDto;
import com.javacode.statment.dto.LoanStatementRequestDto;
import com.javacode.statment.handler.StatementProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatementService {

    private final ValidationService validationService;
    private final RestClient restClient;

    @Value("${deal.service.base-url}")
    private String baseUrl;

    @Value("${deal.service.get-loan-offers-url}")
    private String getLoanOffersUrl;

    @Value("${deal.service.select-offer-url}")
    private String selectOfferUrl;

    public List<LoanOfferDto> getLoanOffers(LoanStatementRequestDto lsrDto) {
        validationService.preScoring(lsrDto);

        return restClient.post()
                .uri(getLoanOffersUrl)
                .body(lsrDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new StatementProcessingException("Deal service returned error: " + response.getStatusCode());
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    public void selectOffer(LoanOfferDto loanOfferDto) {
        validationService.validateLoanOffer(loanOfferDto);

        restClient.post()
                .uri(baseUrl + selectOfferUrl)
                .body(loanOfferDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new StatementProcessingException("Calculator service returned error: " + response.getStatusCode());
                });
    }
}
