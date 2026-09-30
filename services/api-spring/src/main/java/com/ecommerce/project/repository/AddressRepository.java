package com.ecommerce.project.repository;

import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);

    Optional<Address> findByIdAndUser(Long id, User user);
}
