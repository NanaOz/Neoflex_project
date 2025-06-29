package com.javacode.deal.dto.dtoMapper;

import com.javacode.deal.dto.CreditDto;
import com.javacode.deal.dto.PaymentScheduleElementDto;
import com.javacode.deal.model.entity.Credit;
import com.javacode.deal.model.jsonb.PaymentScheduleElement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CreditMapper {
    @Mapping(target = "creditId", ignore = true)
    @Mapping(target = "creditStatus", ignore = true)
    Credit toEntity(CreditDto dto);

    @Mapping(target = "paymentSchedule", source = "paymentSchedule")
    CreditDto toDto(Credit entity);

    default List<PaymentScheduleElement> mapPaymentSchedule(List<PaymentScheduleElementDto> dtos) {
        return dtos.stream()
                .map(dto -> PaymentScheduleElement.builder()
                        .number(dto.getNumber())
                        .date(dto.getDate())
                        .totalPayment(dto.getTotalPayment())
                        .interestPayment(dto.getInterestPayment())
                        .debtPayment(dto.getDebtPayment())
                        .remainingDebt(dto.getRemainingDebt())
                        .build())
                .collect(Collectors.toList());
    }
}
