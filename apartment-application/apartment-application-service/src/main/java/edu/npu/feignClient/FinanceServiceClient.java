package edu.npu.feignClient;

import edu.npu.feignClient.fallback.FinanceServiceClientFallbackFactory;
import edu.npu.feignClient.fallback.ManagementServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "finance-api",
        path = "/finance/remote",
        fallbackFactory = FinanceServiceClientFallbackFactory.class)
public interface FinanceServiceClient {
    @PostMapping("/deposit/{userId}")
    boolean addDepositCharge(@PathVariable(value = "userId") Long userId);
}
