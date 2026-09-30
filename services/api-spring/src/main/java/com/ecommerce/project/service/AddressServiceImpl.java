package com.ecommerce.project.service;

import com.ecommerce.project.dto.AddressCreateRequest;
import com.ecommerce.project.dto.AddressDetailResponse;
import com.ecommerce.project.dto.AddressListResponse;
import com.ecommerce.project.dto.AddressUpdateRequest;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repository.AddressRepository;
import com.ecommerce.project.security.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public AddressListResponse getAllAddresses(Long userId) {
        User user = fetchUser(userId);
        List<Address> addresses = addressRepository.findByUser(user);
        List<AddressDetailResponse> responses = addresses.stream()
                .map(address -> modelMapper.map(address, AddressDetailResponse.class))
                .toList();
        return new AddressListResponse(responses);
    }

    @Override
    public AddressDetailResponse getAddressById(Long userId, Long id) {
        User user = fetchUser(userId);
        Address address = addressRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));
        return modelMapper.map(address, AddressDetailResponse.class);
    }

    @Override
    public AddressDetailResponse createAddress(Long userId, AddressCreateRequest request) {
        User user = fetchUser(userId);
        Address address = modelMapper.map(request, Address.class);
        address.setUser(user);
        Address savedAddress = addressRepository.save(address);
        return modelMapper.map(savedAddress, AddressDetailResponse.class);
    }

    @Override
    public AddressDetailResponse updateAddress(Long userId, Long id, AddressUpdateRequest request) {
        User user = fetchUser(userId);
        Address address = addressRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));

        address.setStreetLine1(request.getStreetLine1());
        address.setStreetLine2(request.getStreetLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setZipCode(request.getZipCode());

        return modelMapper.map(addressRepository.save(address), AddressDetailResponse.class);
    }

    @Override
    public void deleteAddress(Long userId, Long id) {
        User user = fetchUser(userId);
        Address address = addressRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));
        addressRepository.delete(address);
    }

    private User fetchUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}
