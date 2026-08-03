package com.bornik.cashlens.processor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ExpenseRepository extends JpaRepository<Expense, Long> {
}
