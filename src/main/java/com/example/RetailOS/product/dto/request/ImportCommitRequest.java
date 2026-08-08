package com.example.RetailOS.product.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ImportCommitRequest {

    @NotEmpty
    @Valid
    private List<ImportRowRequest> rows;
}
