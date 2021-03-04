package com.leyou.goods.client;

import com.leyou.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ltem-service")
public interface CategoryClient extends CategoryApi {
}
