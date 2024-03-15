package com.customs.network.fdapn.dto;

import lombok.Data;

import java.util.List;
@Data
public class PageDTO<T> {
    private int page;
    private int pageSize;
    private Long totalRecords;
    private List<T> data;
}
