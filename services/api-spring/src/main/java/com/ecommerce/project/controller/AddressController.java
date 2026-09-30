package com.ecommerce.project.controller;

import com.ecommerce.project.config.SwaggerConfig;
import com.ecommerce.project.dto.AddressCreateRequest;
import com.ecommerce.project.dto.AddressDetailResponse;
import com.ecommerce.project.dto.AddressListResponse;
import com.ecommerce.project.dto.AddressUpdateRequest;
import com.ecommerce.project.service.AddressService;
import com.ecommerce.project.util.AuthUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
@Tag(name = SwaggerConfig.Tags.Address.NAME, description = SwaggerConfig.Tags.Address.DESCRIPTION)
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<AddressListResponse> getAllAddresses() {

        Long userId = authUtils.loggedInUser().getId();
        return ResponseEntity.ok(addressService.getAllAddresses(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressDetailResponse> getAddressById(@PathVariable Long id) {
        Long userId = authUtils.loggedInUser().getId();
        return ResponseEntity.ok(addressService.getAddressById(userId, id));
    }

    @PostMapping
    public ResponseEntity<AddressDetailResponse> createAddress(@Valid @RequestBody AddressCreateRequest request) {
        Long userId = authUtils.loggedInUser().getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.createAddress(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressDetailResponse> updateAddress(
            @PathVariable Long id, @Valid @RequestBody AddressUpdateRequest request) {
        Long userId = authUtils.loggedInUser().getId();
        return ResponseEntity.ok(addressService.updateAddress(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        Long userId = authUtils.loggedInUser().getId();
        addressService.deleteAddress(userId, id);
        return ResponseEntity.noContent().build();
    }
}
