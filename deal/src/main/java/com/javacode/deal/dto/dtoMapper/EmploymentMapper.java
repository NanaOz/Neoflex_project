package com.javacode.deal.dto.dtoMapper;

import com.javacode.deal.dto.EmploymentDto;
import com.javacode.deal.model.jsonb.Employment;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmploymentMapper {
    Employment toEntity(EmploymentDto dto);

    EmploymentDto toDto(Employment entity);
}
