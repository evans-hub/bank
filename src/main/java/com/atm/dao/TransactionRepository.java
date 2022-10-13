package com.atm.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;


@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Integer> {

    Transaction save(Transaction t);

    List<Transaction> findAll();

    List<Transaction> findByAccountNo(int accountNo);
    Transaction findTopByOrderByTransactionIdDesc();
}
