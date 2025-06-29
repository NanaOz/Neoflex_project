package com.javacode.deal.dto.dtoMapper;

import com.javacode.deal.dto.EmploymentDto;
import com.javacode.deal.model.enums.EmploymentPosition;
import com.javacode.deal.model.enums.EmploymentStatus;
import com.javacode.deal.model.jsonb.Employment;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmploymentMapper {
    EmploymentDto toDto(Employment entity);

    Employment toEntity(EmploymentDto dto);

    default EmploymentStatus mapEmploymentStatus(String value) {
        return EmploymentStatus.valueOf(value);
    }

    default EmploymentPosition mapEmploymentPosition(String value) {
        return EmploymentPosition.valueOf(value);
    }
}
