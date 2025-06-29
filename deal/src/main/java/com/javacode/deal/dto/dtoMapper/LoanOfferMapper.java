package com.javacode.deal.dto.dtoMapper;


import com.javacode.deal.dto.LoanOfferDto;
import com.javacode.deal.model.jsonb.LoanOffer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LoanOfferMapper {
    LoanOfferDto toDto(LoanOffer entity);

    LoanOffer toEntity(LoanOfferDto dto);
}
