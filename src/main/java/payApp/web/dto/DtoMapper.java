package payApp.web.dto;

import lombok.experimental.UtilityClass;
import payApp.user.model.User;

@UtilityClass
public class DtoMapper {

    public static EditProfileRequest fromUser(User user){

        return EditProfileRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePicture())
                .build();

    }

}
