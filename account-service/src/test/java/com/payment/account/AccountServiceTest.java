package com.payment.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.payment.account.dto.AccountResponse;
import com.payment.account.dto.DebitRequest;
import com.payment.account.dto.DebitResponse;
import com.payment.account.model.Account;
import com.payment.account.repository.AccountRepository;
import com.payment.account.service.AccountService;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
	
	@Mock
	private AccountRepository accountRepository;
	
	@InjectMocks
	private AccountService accountService;
	
	private Account activeAccount;
	private Account blockedAccount;
	
    @BeforeEach
    void setUp() {
        activeAccount = new Account(
                1L,
                "1234567890123456",
                "Alice Johnson",
                500000L,
                200000L,
                "ACTIVE",
                null
        );

        blockedAccount = new Account(
                2L,
                "5678901234567890",
                "Eve Davis",
                50000L,
                100000L,
                "BLOCKED",
                null
        );
    }
    
    @Test
    void getAccount_happyPath_returnsAccountResponse() {
    	when(accountRepository.findByCardNumber("1234567890123456"))
        .thenReturn(Optional.of(activeAccount));
    	
    	AccountResponse response = accountService.getAccount("1234567890123456");
        assertEquals("1234567890123456", response.getCardNumber());
        assertEquals("Alice Johnson", response.getHolderName());
        assertEquals(500000L, response.getBalanceInCents());
        assertEquals("ACTIVE", response.getStatus());
    }
    
    @Test
    void debitAccount_blockedAccount_throwsException() {
        when(accountRepository.findByCardNumber("5678901234567890"))
                .thenReturn(Optional.of(blockedAccount));

        DebitRequest request = new DebitRequest(10000L);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                accountService.debitAccount("5678901234567890", request)
        );

        assertTrue(exception.getMessage().contains("blocked"));
    }
    
    @Test
    void debitAccount_insufficientFunds_throwsException() {
        when(accountRepository.findByCardNumber("1234567890123456"))
                .thenReturn(Optional.of(activeAccount));

        DebitRequest request = new DebitRequest(999999999L);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                accountService.debitAccount("1234567890123456", request)
        );

        assertTrue(exception.getMessage().contains("Insufficient"));
    }
	
    @Test
    void debitAccount_happyPath_reducesBalance() {
        when(accountRepository.findByCardNumber("1234567890123456"))
                .thenReturn(Optional.of(activeAccount));

        DebitRequest request = new DebitRequest(10000L);

        DebitResponse response = accountService.debitAccount("1234567890123456", request);

        assertEquals(490000L, response.getNewBalanceInCents());
        assertEquals("Debit Successful", response.getMessage());
        verify(accountRepository, times(1)).save(activeAccount);
    }

}
