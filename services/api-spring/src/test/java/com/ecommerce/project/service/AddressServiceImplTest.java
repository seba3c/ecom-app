package com.ecommerce.project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.project.dto.AddressCreateRequest;
import com.ecommerce.project.dto.AddressDetailResponse;
import com.ecommerce.project.dto.AddressListResponse;
import com.ecommerce.project.dto.AddressUpdateRequest;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repository.AddressRepository;
import com.ecommerce.project.security.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    @Test
    void getAllAddresses_returnsAddressResponse() {
        User user = new User();
        user.setId(1L);
        Address address1 = new Address();
        address1.setId(1L);
        address1.setStreetLine1("123 Main St");
        address1.setCity("New York");
        address1.setState("NY");
        address1.setCountry("USA");
        address1.setZipCode("10001");
        Address address2 = new Address();
        address2.setId(2L);
        address2.setStreetLine1("456 Oak Ave");
        address2.setCity("Los Angeles");
        address2.setState("CA");
        address2.setCountry("USA");
        address2.setZipCode("90001");
        AddressDetailResponse dto1 =
                new AddressDetailResponse(1L, "123 Main St", null, "New York", "NY", "USA", "10001");
        AddressDetailResponse dto2 =
                new AddressDetailResponse(2L, "456 Oak Ave", null, "Los Angeles", "CA", "USA", "90001");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByUser(user)).thenReturn(List.of(address1, address2));
        when(modelMapper.map(address1, AddressDetailResponse.class)).thenReturn(dto1);
        when(modelMapper.map(address2, AddressDetailResponse.class)).thenReturn(dto2);

        AddressListResponse result = addressService.getAllAddresses(1L);

        assertEquals(2, result.getContent().size());
        assertEquals("123 Main St", result.getContent().get(0).getStreetLine1());
    }

    @Test
    void getAllAddresses_emptyList_returnsEmptyContent() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByUser(user)).thenReturn(List.of());

        AddressListResponse result = addressService.getAllAddresses(1L);

        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void getAddressById_success() {
        User user = new User();
        user.setId(1L);
        Address address = new Address();
        address.setId(1L);
        address.setStreetLine1("123 Main St");
        address.setCity("New York");
        address.setState("NY");
        address.setCountry("USA");
        address.setZipCode("10001");
        AddressDetailResponse dto =
                new AddressDetailResponse(1L, "123 Main St", null, "New York", "NY", "USA", "10001");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(address));
        when(modelMapper.map(address, AddressDetailResponse.class)).thenReturn(dto);

        AddressDetailResponse result = addressService.getAddressById(1L, 1L);

        assertEquals(1L, result.getId());
        assertEquals("123 Main St", result.getStreetLine1());
    }

    @Test
    void getAddressById_notFound_throwsResourceNotFoundException() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> addressService.getAddressById(1L, 99L));
    }

    @Test
    void createAddress_success() {
        User user = new User();
        user.setId(1L);
        AddressCreateRequest request = new AddressCreateRequest("123 Main St", null, "New York", "NY", "USA", "10001");
        Address mappedAddress = new Address();
        mappedAddress.setStreetLine1("123 Main St");
        mappedAddress.setCity("New York");
        mappedAddress.setState("NY");
        mappedAddress.setCountry("USA");
        mappedAddress.setZipCode("10001");
        Address savedAddress = new Address();
        savedAddress.setId(1L);
        savedAddress.setStreetLine1("123 Main St");
        savedAddress.setCity("New York");
        savedAddress.setState("NY");
        savedAddress.setCountry("USA");
        savedAddress.setZipCode("10001");
        AddressDetailResponse resultDTO =
                new AddressDetailResponse(1L, "123 Main St", null, "New York", "NY", "USA", "10001");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(modelMapper.map(request, Address.class)).thenReturn(mappedAddress);
        when(addressRepository.save(mappedAddress)).thenReturn(savedAddress);
        when(modelMapper.map(savedAddress, AddressDetailResponse.class)).thenReturn(resultDTO);

        AddressDetailResponse result = addressService.createAddress(1L, request);

        assertEquals(1L, result.getId());
        assertEquals("123 Main St", result.getStreetLine1());
        verify(addressRepository).save(mappedAddress);
    }

    @Test
    void updateAddress_success() {
        User user = new User();
        user.setId(1L);
        AddressUpdateRequest request = new AddressUpdateRequest("Updated St", null, "Boston", "MA", "USA", "02101");
        Address existingAddress = new Address();
        existingAddress.setId(1L);
        existingAddress.setStreetLine1("123 Main St");
        AddressDetailResponse resultDTO =
                new AddressDetailResponse(1L, "Updated St", null, "Boston", "MA", "USA", "02101");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(existingAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(existingAddress);
        when(modelMapper.map(existingAddress, AddressDetailResponse.class)).thenReturn(resultDTO);

        AddressDetailResponse result = addressService.updateAddress(1L, 1L, request);

        assertEquals(1L, result.getId());
        assertEquals("Updated St", result.getStreetLine1());
        assertEquals("Updated St", existingAddress.getStreetLine1());
        assertNull(existingAddress.getStreetLine2());
        assertEquals("Boston", existingAddress.getCity());
        assertEquals("MA", existingAddress.getState());
        assertEquals("USA", existingAddress.getCountry());
        assertEquals("02101", existingAddress.getZipCode());
        verify(addressRepository).save(existingAddress);
    }

    @Test
    void updateAddress_notFound_throwsResourceNotFoundException() {
        User user = new User();
        user.setId(1L);
        AddressUpdateRequest request = new AddressUpdateRequest("Updated St", null, "Boston", "MA", "USA", "02101");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> addressService.updateAddress(1L, 99L, request));
    }

    @Test
    void deleteAddress_success() {
        User user = new User();
        user.setId(1L);
        Address address = new Address();
        address.setId(1L);
        address.setStreetLine1("123 Main St");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(address));

        addressService.deleteAddress(1L, 1L);

        verify(addressRepository).delete(address);
    }

    @Test
    void deleteAddress_notFound_throwsResourceNotFoundException() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> addressService.deleteAddress(1L, 99L));
    }
}
