package com.project.shopapp.repositories;

import com.project.shopapp.models.Entities.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

}
