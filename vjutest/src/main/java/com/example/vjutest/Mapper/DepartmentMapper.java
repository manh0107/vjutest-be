package com.example.vjutest.Mapper;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.example.vjutest.DTO.DepartmentDTO;
import com.example.vjutest.Model.Department;

@Component
public class DepartmentMapper {
    public DepartmentDTO toDTO(Department department) {
        if (department == null) {
            return null;
        }

        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setCreatedAt(department.getCreatedAt());
        dto.setModifiedAt(department.getModifiedAt());

        if (department.getCreatedBy() != null) {
            dto.setCreatedById(department.getCreatedBy().getId());
            dto.setCreatedByName(department.getCreatedBy().getName());
        }

        if (department.getModifiedBy() != null) {
            dto.setModifiedById(department.getModifiedBy().getId());
            dto.setModifiedByName(department.getModifiedBy().getName());
        }

        return dto;
    }    

    public List<DepartmentDTO> toDTOList(List<Department> departments) {
        if (departments == null) {
            return null;
        }
        return departments.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
