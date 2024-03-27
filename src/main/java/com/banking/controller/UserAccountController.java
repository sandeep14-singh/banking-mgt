package com.banking.controller;

import com.banking.dto.UserDto;
import com.banking.entity.BankAccount;
import com.banking.entity.UserAccount;
import com.banking.repository.BankAccountRepository;
import com.banking.repository.UserAccountRepository;
import com.banking.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserAccountController {

    @Autowired
    UserAccountRepository userAccountRepository;
    @Autowired
    BankAccountRepository bankAccountRepository;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;
    @PostMapping("/signUp")
    public ResponseEntity signup(@RequestBody UserDto userDTO) {
        UserAccount userAccount = new UserAccount();
        userAccount.setEmail(userDTO.getEmail());
        userAccount.setPassword(userDTO.getPassword());
        userAccount.setRole(userDTO.getRole().name());
        userAccountRepository.save(userAccount);
        BankAccount bankAccount = new BankAccount();
        bankAccount.setEmail(userDTO.getEmail());
        bankAccount.setBalance(0l);
        bankAccountRepository.save(bankAccount);
        return ResponseEntity.ok().body("Registration Successful!");
    }

    @PostMapping("/signIn")
    public ResponseEntity signin(@RequestBody UserDto userDTO) {
        GrantedAuthority grantedAuthority = new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userDTO.getRole().name();
            }
        };
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(userDTO.getEmail(), userDTO.getPassword(), List.of(grantedAuthority)));
        // call to  authenticationManager.authenticate performs authentication
        // As part of this, userdetailservice is called by passing username to it and password returned from there is matched with what we are supplying here
        // If it matches, its authenticated. Roles/Granted Authority passed here are not matched with what is returned from there & hence we can even avoid passing it in the 3 rd arg of UsernamePasswordAuthenticationToken's constructor
        // If it does not matches, it throws exception
        String token = jwtUtil.createToken(userDTO);
        return ResponseEntity.ok().body(token);
    }
}
