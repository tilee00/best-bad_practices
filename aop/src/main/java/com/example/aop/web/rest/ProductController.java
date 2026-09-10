package com.example.aop.web.rest;

import com.example.aop.dto.product.ProductReq;
import com.example.aop.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final IProductService iService;

    // @RequireRole(Role.ADMIN), @AuditLog(action = "CREATE_PRODUCT")
    @PostMapping
    public ResponseEntity<Void> create(@RequestBody ProductReq productReq) {
//        iService.saveAboutUs(productReq);
        return ResponseEntity.ok().body(null);
    }

}
