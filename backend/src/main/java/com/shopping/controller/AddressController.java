package com.shopping.controller;

import com.shopping.dto.AddressRequest;
import com.shopping.dto.Result;
import com.shopping.entity.Address;
import com.shopping.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public Result<List<Address>> getAddresses(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return addressService.getAddresses(userId);
    }

    @PostMapping
    public Result<Address> addAddress(Authentication authentication, @Valid @RequestBody AddressRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return addressService.addAddress(userId, request);
    }

    @PutMapping("/{id}")
    public Result<Address> updateAddress(Authentication authentication, @PathVariable Long id,
                                         @Valid @RequestBody AddressRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return addressService.updateAddress(userId, id, request);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAddress(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return addressService.deleteAddress(userId, id);
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return addressService.setDefault(userId, id);
    }
}
