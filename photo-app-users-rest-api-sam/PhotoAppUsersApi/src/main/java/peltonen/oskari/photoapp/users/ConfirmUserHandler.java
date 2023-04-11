package peltonen.oskari.photoapp.users;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import peltonen.oskari.photoapp.users.service.CognitoUserService;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

public class ConfirmUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final CognitoUserService cognitoUserService;
    private final String appClientSecret;
    private final String appClientId;

    public ConfirmUserHandler() {
        this.cognitoUserService = new CognitoUserService(System.getenv("AWS_REGION"));
        this.appClientId = Utils.decryptKey("MY_COGNITO_POOL_APP_CLIENT_ID");
        this.appClientSecret = Utils.decryptKey("MY_COGNITO_POOL_APP_CLIENT_SECRET");
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        LambdaLogger logger = context.getLogger();

        try {
            String requestBodyJsonString = input.getBody();
            JsonObject requestBody = JsonParser.parseString(requestBodyJsonString).getAsJsonObject();
            String email = requestBody.get("email").getAsString();
            String confirmationCode = requestBody.get("code").getAsString();

            JsonObject confirmUserResult = cognitoUserService.confirmUserSignup(appClientId, appClientSecret, email, confirmationCode);
            logger.log("aws response statusCode: " + confirmUserResult.get("statusCode"));
            response.withStatusCode(200);
            response.withBody(new Gson().toJson(confirmUserResult, JsonObject.class));
        } catch (AwsServiceException e) {
            logger.log("aws error statusCode: " +e.awsErrorDetails().errorCode());
            logger.log(e.awsErrorDetails().errorMessage());
            response.withBody(e.awsErrorDetails().errorMessage());
            response.withStatusCode(e.awsErrorDetails().sdkHttpResponse().statusCode());
        } catch (Exception e) {
            logger.log(e.getMessage());
            response.withBody(e.getMessage());
            response.withStatusCode(500);
        }
        return response;
    }
}
