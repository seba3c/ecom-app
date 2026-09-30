package com.ecommerce.project.service;

import com.ecommerce.project.dto.AddressCreateRequest;
import com.ecommerce.project.dto.AddressDetailResponse;
import com.ecommerce.project.dto.AddressListResponse;
import com.ecommerce.project.dto.AddressUpdateRequest;

public interface AddressService {

    AddressListResponse getAllAddresses(Long userId);

    AddressDetailResponse getAddressById(Long userId, Long id);

    AddressDetailResponse createAddress(Long userId, AddressCreateRequest request);

    AddressDetailResponse updateAddress(Long userId, Long id, AddressUpdateRequest request);

    void deleteAddress(Long userId, Long id);
}
