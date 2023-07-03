package edu.npu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

//UserPayListQueryDto
public record UserPayListQueryDto(
        @NotNull(message = "pageNum不能为空")
        Integer pageNum,

        @NotNull(message = "pageSize不能为空")
        Integer pageSize,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        Date beginTime,

        Integer type,

        Integer status,

        Long departmentId
) {
}
