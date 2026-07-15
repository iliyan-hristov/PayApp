package payApp.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import payApp.user.model.User;
import payApp.user.property.UserProperties;
import payApp.web.dto.RegisterRequest;

import java.util.List;

@Component
public class UserInit implements ApplicationRunner {
    private final UserService userService;
    private final UserProperties userProperties;

    @Autowired
    public UserInit(UserService userService, UserProperties userProperties) {
        this.userService = userService;
        this.userProperties = userProperties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        List<User> users = userService.getAllUsers();

        boolean defaultUserDoesNotExist = users.stream().noneMatch(user -> user.getUsername().equals(userProperties.getDefaultUser().getUsername()));

        if (defaultUserDoesNotExist) {

            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username(userProperties.getDefaultUser().getUsername())
                    .password(userProperties.getDefaultUser().getPassword())
                    .country(userProperties.getDefaultUser().getCountry())
                    .build();
            userService.register(registerRequest);
        }
    }

}
