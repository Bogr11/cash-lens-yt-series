package com.bornik.cashlens.processor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByAccountId(String accountId);

    Optional<Expense> findByAccountIdAndMerchantOrderByCreatedDateDesc(String accountId, String merchant);

}