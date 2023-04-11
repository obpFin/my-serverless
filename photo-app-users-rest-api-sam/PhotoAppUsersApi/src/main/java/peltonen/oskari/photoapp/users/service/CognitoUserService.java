package peltonen.oskari.photoapp.users.service;

import com.google.gson.JsonObject;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentity.model.CognitoIdentityProvider;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class CognitoUserService {

    private CognitoIdentityProviderClient cognitoIdentityProviderClient;

    public CognitoUserService(String region) {
        this.cognitoIdentityProviderClient = CognitoIdentityProviderClient
                .builder()
                .region(Region.of(region))
                .build();
    }

    public CognitoUserService(CognitoIdentityProviderClient cognitoIdentityProviderClient) {
        this.cognitoIdentityProviderClient = cognitoIdentityProviderClient;
    }

    public JsonObject confirmUserSignup(String appClientId,
                                  String appClientSecret,
                                  String email,
                                  String confirmationCode) {
        String generatedSecretHash = calculateSecretHash(appClientId, appClientSecret, email);

        ConfirmSignUpRequest confirmSignUpRequest = ConfirmSignUpRequest.builder()
                .secretHash(generatedSecretHash)
                .username(email)
                .confirmationCode(confirmationCode)
                .clientId(appClientId)
                .build();

        ConfirmSignUpResponse confirmSignUpResponse = cognitoIdentityProviderClient.confirmSignUp(confirmSignUpRequest);

        JsonObject confirmUserSignupResponse = new JsonObject();
        confirmUserSignupResponse.addProperty("isSuccessful", confirmSignUpResponse.sdkHttpResponse().isSuccessful());
        confirmUserSignupResponse.addProperty("statusCode",  confirmSignUpResponse.sdkHttpResponse().statusCode());
        return confirmUserSignupResponse;
    }

    public JsonObject createUser(JsonObject user, String appClientId, String appClientSecret) {
        String email = user.get("email").getAsString();
        String pwd = user.get("password").getAsString();
        String userId = UUID.randomUUID().toString();
        String firstName = user.get("firstName").getAsString();
        String lastName = user.get("lastName").getAsString();


        AttributeType emailAttribute = AttributeType.builder()
                .name("email")
                .value(email)
                .build();

        AttributeType nameAtribute = AttributeType.builder()
                .name("name")
                .value(firstName + lastName)
                .build();

        AttributeType userIdAttribute = AttributeType.builder()
                .name("custom:userId")
                .value(userId)
                .build();

        List<AttributeType> attributes = new ArrayList<>();
        attributes.add(emailAttribute);
        attributes.add(nameAtribute);
        attributes.add(userIdAttribute);

        String generatedSecretHash = calculateSecretHash(appClientId, appClientSecret, email);

        SignUpRequest signUpRequest = SignUpRequest.builder()
                .username(email)
                .password(pwd)
                .userAttributes(attributes)
                .clientId(appClientId)
                .secretHash(generatedSecretHash)
                .build();

        SignUpResponse response = cognitoIdentityProviderClient.signUp(signUpRequest);
        JsonObject createUserResult = new JsonObject();
        createUserResult.addProperty("isSuccessful", response.sdkHttpResponse().isSuccessful());
        createUserResult.addProperty("statusCode", response.sdkHttpResponse().statusCode());
        createUserResult.addProperty("cognitoUserId", response.userSub());
        createUserResult.addProperty("isConfirmed", response.userConfirmed());

        return createUserResult;
    }

    public String calculateSecretHash(String userPoolClientId, String userPoolClientSecret, String userName) {
        final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

        SecretKeySpec signingKey = new SecretKeySpec(
                userPoolClientSecret.getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256_ALGORITHM);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            mac.update(userName.getBytes(StandardCharsets.UTF_8));
            byte[] rawHmac = mac.doFinal(userPoolClientId.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating ");
        }
    }
}
