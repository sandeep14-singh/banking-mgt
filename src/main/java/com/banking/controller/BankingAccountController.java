package com.banking.controller;

import com.banking.dto.BankingDto;
import com.banking.dto.RequestType;
import com.banking.entity.BankAccount;
import com.banking.repository.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BankingAccountController {

    @Autowired
    BankAccountRepository bankAccountRepository;

    @PostMapping("/service")
    public ResponseEntity deposit(@RequestBody BankingDto bankingDto) {
        BankAccount bankAccount = bankAccountRepository.findByEmail(bankingDto.getEmail());
        Long existingBalance = bankAccount.getBalance();

        if (bankingDto.getRequestType().equals(RequestType.DEPOSIT)) {
            bankAccount.setBalance(existingBalance + bankingDto.getAmount());
            bankAccountRepository.save(bankAccount);
            return ResponseEntity.ok().body("Deposited!");
        }

        if (existingBalance - bankingDto.getAmount() < 0 ) {
            return ResponseEntity.ok().body("Cannot Withdraw. Account is short of money");
        } else {
            bankAccount.setBalance(existingBalance - bankingDto.getAmount());
            bankAccountRepository.save(bankAccount);
            return ResponseEntity.ok().body("Withdrawn!");
        }

    }

}
