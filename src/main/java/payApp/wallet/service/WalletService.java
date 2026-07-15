package payApp.wallet.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import payApp.transaction.model.Transaction;
import payApp.transaction.model.TransactionStatus;
import payApp.transaction.model.TransactionType;
import payApp.transaction.service.TransactionService;
import payApp.user.model.User;
import payApp.wallet.model.Wallet;
import payApp.wallet.model.WalletStatus;
import payApp.wallet.repository.WalletRepository;
import payApp.web.dto.TransferRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

@Service
public class WalletService {

    private final static String SENDER_IDENTIFIER = "payApp platform";
    private final static String FAILURE_REASON = "Inactive Wallet";
    private final static String FAILURE_REASON_NO_FUNDS = "Insufficient Funds";
    private final static String FAILURE_REASON_NO_OWNED_WALLET = "You don't own this wallet";

    private final WalletRepository walletRepository;
    private final TransactionService transactionService;

    @Autowired
    public WalletService(WalletRepository walletRepository, TransactionService transactionService) {
        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
    }

    @Transactional
    public Transaction withdrawal (User user, UUID walletId, BigDecimal amount, String description){

        Wallet wallet = getById(walletId);

        Transaction transaction = Transaction.builder()
                .owner(user)
                .sender(wallet.getId().toString())
                .receiver(SENDER_IDENTIFIER)
                .amount(amount)
                .currency(wallet.getCurrency())
                .type(TransactionType.WITHDRAWAL)
                .description(description)
                .createdOn(LocalDateTime.now())
                .build();

        if (!isActiveWallet(wallet)){
            transaction.setFailureReason(FAILURE_REASON);
            transaction.setStatus(TransactionStatus.FAILED);
        } else if (!hasSufficientFunds(wallet, amount)){
            transaction.setFailureReason(FAILURE_REASON_NO_FUNDS);
            transaction.setStatus(TransactionStatus.FAILED);
        } else if (!isWalletOwnedByUser(wallet, user)) {
            transaction.setFailureReason(FAILURE_REASON_NO_OWNED_WALLET);
            transaction.setStatus(TransactionStatus.FAILED);
        } else{
            transaction.setStatus(TransactionStatus.SUCCEEDED);
            wallet.setBalance(wallet.getBalance().subtract(amount));
            wallet.setUpdatedOn(LocalDateTime.now());
            walletRepository.save(wallet);
        }

        transaction.setBalanceLeft(wallet.getBalance());

        return transactionService.upsert(transaction);

    }

    public boolean isWalletOwnedByUser(Wallet wallet, User user){

        return wallet.getOwner().getId().equals(user.getId());
    }

    public boolean isActiveWallet (Wallet wallet){

        return wallet.getStatus() == WalletStatus.ACTIVE;
    }

    public boolean hasSufficientFunds(Wallet wallet, BigDecimal amount){

        BigDecimal a = wallet.getBalance();
        BigDecimal b = amount;

        return a.compareTo(b) >= 0;
    }



    @Transactional
    public Transaction deposit(UUID walletId, BigDecimal topUpAmount, String description){

        Wallet wallet = getById(walletId);
        //String transactionDescription = "Top-up %.2f".formatted(topUpAmount.doubleValue());

        if (wallet.getStatus() == WalletStatus.INACTIVE){

            return transactionService.createNewTransaction(wallet.getOwner(),
                    SENDER_IDENTIFIER,
                    wallet.getId().toString(),
                    topUpAmount,
                    wallet.getBalance(),
                    wallet.getCurrency(),
                    TransactionType.DEPOSIT,
                    TransactionStatus.FAILED,
                    description,
                    FAILURE_REASON);

        }

        wallet.setBalance(wallet.getBalance().add(topUpAmount));
        wallet.setUpdatedOn(LocalDateTime.now());

        walletRepository.save(wallet);

        return transactionService.createNewTransaction(wallet.getOwner(),
                SENDER_IDENTIFIER,
                wallet.getId().toString(),
                topUpAmount,
                wallet.getBalance(),
                wallet.getCurrency(),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCEEDED,
                description,
                null);

    }

    public Wallet createDefaultWallet(User user) {

         Wallet wallet = Wallet.builder()
                  .owner(user)
                 .nickname("Default")
                  .status(WalletStatus.ACTIVE)
                  .balance(new BigDecimal("20.00"))
                  .currency(Currency.getInstance("EUR"))
                 .main(true)
                  .createdOn(LocalDateTime.now())
                  .updatedOn(LocalDateTime.now())
                  .build();

         return walletRepository.save(wallet);
    }


    private Wallet getById(UUID walletId) {

        return walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("Wallet was not found!"));
    }

    @Transactional
    public Transaction transfer(TransferRequest transferRequest) {

        Wallet senderWallet = getById(transferRequest.getWalletId());
        Wallet receiverWallet = getFirstByUsername(transferRequest.getRecipientUserName());

        String transferDescription = "Transfer [%s] <> [%s] [%.2f]".formatted(senderWallet.getOwner().getUsername(), receiverWallet.getOwner().getUsername(), transferRequest.getAmount());
        Transaction withdrawalTransaction = withdrawal(senderWallet.getOwner(), senderWallet.getId(), transferRequest.getAmount(), transferDescription);

        if (withdrawalTransaction.getStatus() == TransactionStatus.SUCCEEDED){
            deposit(receiverWallet.getId(), transferRequest.getAmount(), transferDescription);

        }

        return withdrawalTransaction;
    }

    private Wallet getFirstByUsername(String recipientUsername) {

        return walletRepository.findByOwnerUsername(recipientUsername).stream()
                .filter(wallet -> isActiveWallet(wallet))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("[%s] doesn't any active wallets.".formatted(recipientUsername)));
    }
}
