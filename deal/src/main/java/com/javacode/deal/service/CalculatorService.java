package com.javacode.deal.service;

import com.javacode.deal.dto.CreditDto;
import com.javacode.deal.dto.LoanOfferDto;
import com.javacode.deal.dto.LoanStatementRequestDto;
import com.javacode.deal.dto.ScoringDataDto;
import com.javacode.deal.exeption.CalculatorServiceException;
import com.javacode.deal.exeption.CreditRequestFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculatorService {

    private final RestClient restClient;

    @Value("${calculator.service.calc-offers-url}")
    private String offerURL;

    @Value("${calculator.service.calc-credit-url}")
    private String calculatorURL;

    public List<LoanOfferDto> getOffers(LoanStatementRequestDto requestDto) {
        try {
            return restClient.post()
                    .uri(offerURL)
                    .body(requestDto)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, responce) -> {
                        throw new CalculatorServiceException("Calculate вернул ошибку: "
                                + responce.getStatusCode() + " возвращенное сообщение: " + responce);
                    })
                    .body(new ParameterizedTypeReference<List<LoanOfferDto>>() {
                    });
        } catch (RestClientException e) {
            throw new CalculatorServiceException("Не удалось вызвать Calculate : " + e.getMessage());
        }
    }

    public CreditDto getCredit(ScoringDataDto scoringData) throws CreditRequestFailedException {
        CreditDto creditDto = restClient.post()
                .uri(calculatorURL)
                .body(scoringData)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new CalculatorServiceException("Calculator вернул ошибку: "
                            + response.getStatusCode() + "\n, возвращенное сообщение: " + response);
                })
                .body(CreditDto.class);
        if (creditDto == null) {
            throw new CreditRequestFailedException("Не удалось рассчитать кредит, получено значение NULL из ms calculator");
        } else return creditDto;
    }
}
