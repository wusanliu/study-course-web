package com.xuecheng.content.feginclient;

import com.xuecheng.content.model.po.CourseIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Xue
 * @create 2023-09-13-9:34
 */
@FeignClient(value = "search",fallbackFactory = SearchServiceClientFactory.class)
public interface SearchServiceClient {
    @PostMapping("/search/index/course")
    public Boolean add(@RequestBody CourseIndex courseIndex);
}
