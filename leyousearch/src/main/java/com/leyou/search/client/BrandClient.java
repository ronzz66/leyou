package com.leyou.search.client;

import com.leyou.item.api.BranApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ltem-service")
public interface BrandClient extends BranApi {
}
