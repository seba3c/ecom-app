package com.ecommerce.project.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.project.dto.AddressCreateRequest;
import com.ecommerce.project.dto.AddressDetailResponse;
import com.ecommerce.project.dto.AddressListResponse;
import com.ecommerce.project.dto.AddressUpdateRequest;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.User;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.service.UserDetailsServiceImpl;
import com.ecommerce.project.service.AddressService;
import com.ecommerce.project.util.AuthUtils;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AddressController.class)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressService addressService;

    @MockitoBean
    private AuthUtils authUtils;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        when(authUtils.loggedInUser()).thenReturn(user);
    }

    @Test
    void getAllAddresses_returns200WithAddresses() throws Exception {
        AddressDetailResponse dto1 =
                new AddressDetailResponse(1L, "123 Main St", null, "New York", "NY", "USA", "10001");
        AddressDetailResponse dto2 =
                new AddressDetailResponse(2L, "456 Oak Ave", null, "Los Angeles", "CA", "USA", "90001");
        AddressListResponse response = new AddressListResponse(List.of(dto1, dto2));
        when(addressService.getAllAddresses(anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].streetLine1").value("123 Main St"))
                .andExpect(jsonPath("$.content[1].streetLine1").value("456 Oak Ave"));
    }

    @Test
    void getAllAddresses_empty_returns200() throws Exception {
        when(addressService.getAllAddresses(anyLong())).thenReturn(new AddressListResponse(List.of()));

        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void getAddressById_returns200() throws Exception {
        AddressDetailResponse dto =
                new AddressDetailResponse(1L, "123 Main St", null, "New York", "NY", "USA", "10001");
        when(addressService.getAddressById(anyLong(), eq(1L))).thenReturn(dto);

        mockMvc.perform(get("/api/addresses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.streetLine1").value("123 Main St"))
                .andExpect(jsonPath("$.city").value("New York"))
                .andExpect(jsonPath("$.state").value("NY"))
                .andExpect(jsonPath("$.country").value("USA"))
                .andExpect(jsonPath("$.zipCode").value("10001"));
    }

    @Test
    void getAddressById_notFound_returns404() throws Exception {
        when(addressService.getAddressById(anyLong(), eq(99L)))
                .thenThrow(new ResourceNotFoundException("Address", "id", 99L));

        mockMvc.perform(get("/api/addresses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Address with id: 99 not found"));
    }

    @Test
    void createAddress_returns201() throws Exception {
        AddressDetailResponse resultDTO =
                new AddressDetailResponse(1L, "123 Main St", null, "New York", "NY", "USA", "10001");
        when(addressService.createAddress(anyLong(), any(AddressCreateRequest.class)))
                .thenReturn(resultDTO);

        mockMvc.perform(
                        post("/api/addresses")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                    "streetLine1": "123 Main St",
                                    "city": "New York",
                                    "state": "New York",
                                    "country": "USA",
                                    "zipCode": "10001"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.streetLine1").value("123 Main St"))
                .andExpect(jsonPath("$.city").value("New York"))
                .andExpect(jsonPath("$.state").value("NY"))
                .andExpect(jsonPath("$.country").value("USA"))
                .andExpect(jsonPath("$.zipCode").value("10001"));
    }

    @Test
    void updateAddress_returns200() throws Exception {
        AddressDetailResponse resultDTO =
                new AddressDetailResponse(1L, "Updated St", null, "Boston", "MA", "USA", "02101");
        when(addressService.updateAddress(anyLong(), eq(1L), any(AddressUpdateRequest.class)))
                .thenReturn(resultDTO);

        mockMvc.perform(
                        put("/api/addresses/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                    "streetLine1": "Updated St",
                                    "city": "Boston",
                                    "state": "Massachusetts",
                                    "country": "USA",
                                    "zipCode": "02101"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.streetLine1").value("Updated St"))
                .andExpect(jsonPath("$.city").value("Boston"))
                .andExpect(jsonPath("$.state").value("MA"))
                .andExpect(jsonPath("$.country").value("USA"))
                .andExpect(jsonPath("$.zipCode").value("02101"));
    }

    @Test
    void updateAddress_notFound_returns404() throws Exception {
        when(addressService.updateAddress(anyLong(), eq(99L), any(AddressUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Address", "id", 99L));

        mockMvc.perform(
                        put("/api/addresses/99")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                    "streetLine1": "Updated St",
                                    "city": "Boston",
                                    "state": "Massachusetts",
                                    "country": "USA",
                                    "zipCode": "02101"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Address with id: 99 not found"));
    }

    @Test
    void deleteAddress_returns204() throws Exception {
        doNothing().when(addressService).deleteAddress(anyLong(), eq(1L));

        mockMvc.perform(delete("/api/addresses/1")).andExpect(status().isNoContent());
    }

    @Test
    void deleteAddress_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Address", "id", 99L))
                .when(addressService)
                .deleteAddress(anyLong(), eq(99L));

        mockMvc.perform(delete("/api/addresses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Address with id: 99 not found"));
    }
}
