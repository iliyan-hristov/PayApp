package payApp.user.service;

import jakarta.transaction.Transaction;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import payApp.subscription.model.Subscription;
import payApp.subscription.service.SubscriptionService;
import payApp.user.model.User;
import payApp.user.model.UserRole;
import payApp.user.property.UserProperties;
import payApp.user.repository.UserRepository;
import payApp.wallet.model.Wallet;
import payApp.wallet.service.WalletService;
import payApp.web.dto.EditProfileRequest;
import payApp.web.dto.LoginRequest;
import payApp.web.dto.RegisterRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;
    private final SubscriptionService subscriptionService;
    private final UserProperties userProperties;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, WalletService walletService, SubscriptionService subscriptionService, UserProperties userProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
        this.subscriptionService = subscriptionService;
        this.userProperties = userProperties;
    }

    public User login(LoginRequest loginRequest){

        Optional<User> optionalUser = userRepository.findByUsername(loginRequest.getUsername());
        if(optionalUser.isEmpty()){
            throw new RuntimeException("Invalid password or username!");
        }

        String rawPassword = loginRequest.getPassword();
        String hashedPassword = optionalUser.get().getPassword();

        if (!passwordEncoder.matches(rawPassword, hashedPassword)){
            throw new RuntimeException("Invalid password or username!");
        }

        return optionalUser.get();
    }

    @Transactional
    public User register(RegisterRequest registerRequest) {

        Optional<User> optionalUser = userRepository.findByUsername(registerRequest.getUsername());

        if (optionalUser.isPresent()) {
            throw new RuntimeException("User [%s] already exists!".formatted(registerRequest.getUsername()));
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .country(registerRequest.getCountry())
                .role(UserRole.USER)
                .active(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        user = userRepository.save(user);
        Wallet defaultWallet = walletService.createDefaultWallet(user);
        Subscription defaultSubscription = subscriptionService.createDefaultSubscription(user);

        user.setWallets(List.of(defaultWallet));
        user.setSubscriptions(List.of(defaultSubscription));

        log.info("User [%s] successfully registered!".formatted(registerRequest.getUsername()));

        return user;

    }

    public List<User> getAllUsers() {

        return userRepository.findAll();

    }


    public User getByUsername(String username) {

        return userRepository.findByUsername(username).orElseThrow(()-> new RuntimeException("User not found!"));
    }

    public User getById(UUID id) {

        return userRepository.findById(id).orElseThrow(()-> new RuntimeException("User not found!"));
    }

    public User getDefaultUser() {

        return getByUsername(userProperties.getDefaultUser().getUsername());
    }

    public void updateProfile(UUID id, EditProfileRequest editProfileRequest) {

        User user = getById(id);

        user.setFirstName(editProfileRequest.getFirstName());
        user.setLastName(editProfileRequest.getLastName());
        user.setEmail(editProfileRequest.getEmail());
        user.setProfilePicture(editProfileRequest.getProfilePictureUrl());

        userRepository.save(user);

    }
}
