package com.xuecheng.base.model;

import lombok.Data;
import lombok.ToString;

/**
 * @author Xue
 * @create 2023-09-03-10:59
 */
@Data
@ToString
public class PageParams {
    private Long pageNo=1L;
    private Long pageSize=30L;

    public PageParams() {
    }

    public PageParams(Long pageNo, Long pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }
}
