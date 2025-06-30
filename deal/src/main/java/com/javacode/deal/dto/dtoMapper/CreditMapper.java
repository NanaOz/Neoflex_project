package com.javacode.deal.dto.dtoMapper;

import com.javacode.deal.dto.CreditDto;
import com.javacode.deal.dto.PaymentScheduleElementDto;
import com.javacode.deal.model.entity.Credit;
import com.javacode.deal.model.jsonb.PaymentScheduleElement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CreditMapper {
    @Mapping(target = "creditId", ignore = true)
    @Mapping(target = "creditStatus", ignore = true)
    @Mapping(target = "insuranceEnabled", expression = "java(dto.getIsInsuranceEnabled())")
    @Mapping(target = "salaryClient", expression = "java(dto.getIsSalaryClient())")
    @Mapping(target = "paymentSchedule", source = "paymentSchedule", qualifiedByName = "mapPaymentSchedule")
    Credit toEntity(CreditDto dto);

    @Mapping(target = "isInsuranceEnabled", expression = "java(entity.getInsuranceEnabled())")
    @Mapping(target = "isSalaryClient", expression = "java(entity.getSalaryClient())")
    @Mapping(target = "paymentSchedule", source = "paymentSchedule", qualifiedByName = "mapPaymentScheduleDto")
    CreditDto toDto(Credit entity);

    @Named("mapPaymentSchedule")
    default List<PaymentScheduleElement> mapPaymentSchedule(List<PaymentScheduleElementDto> dtos) {
        if (dtos == null) return null;
        return dtos.stream().map(this::toPaymentElement).toList();
    }

    @Named("mapPaymentScheduleDto")
    default List<PaymentScheduleElementDto> mapPaymentScheduleDto(List<PaymentScheduleElement> elements) {
        if (elements == null) return null;
        return elements.stream().map(this::toPaymentElementDto).toList();
    }

    @Mapping(target = "number", source = "number")
    @Mapping(target = "date", source = "date")
    @Mapping(target = "totalPayment", source = "totalPayment")
    @Mapping(target = "interestPayment", source = "interestPayment")
    @Mapping(target = "debtPayment", source = "debtPayment")
    @Mapping(target = "remainingDebt", source = "remainingDebt")
    PaymentScheduleElement toPaymentElement(PaymentScheduleElementDto dto);

    @Mapping(target = "number", source = "number")
    @Mapping(target = "date", source = "date")
    @Mapping(target = "totalPayment", source = "totalPayment")
    @Mapping(target = "interestPayment", source = "interestPayment")
    @Mapping(target = "debtPayment", source = "debtPayment")
    @Mapping(target = "remainingDebt", source = "remainingDebt")
    PaymentScheduleElementDto toPaymentElementDto(PaymentScheduleElement entity);
}
