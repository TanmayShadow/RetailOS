package com.example.RetailOS.shop.controller;

import com.example.RetailOS.shop.dto.request.CreateShopRequest;
import com.example.RetailOS.shop.dto.request.UpdateShopRequest;
import com.example.RetailOS.shop.dto.response.ShopResponse;
import com.example.RetailOS.shop.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ShopResponse> createShop(@Valid @RequestBody CreateShopRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(shopService.createShop(request));
    }

    @GetMapping("/me")
    public ResponseEntity<ShopResponse> getMyShop() {
        return ResponseEntity.ok(shopService.getMyShop());
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ShopResponse> updateMyShop(@Valid @RequestBody UpdateShopRequest request) {
        return ResponseEntity.ok(shopService.updateMyShop(request));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ShopResponse> getShopBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(shopService.getShopBySlug(slug));
    }

    @GetMapping("/slug-availability")
    public ResponseEntity<Boolean> isSlugAvailable(@RequestParam String slug) {
        return ResponseEntity.ok(shopService.isSlugAvailable(slug));
    }
}
