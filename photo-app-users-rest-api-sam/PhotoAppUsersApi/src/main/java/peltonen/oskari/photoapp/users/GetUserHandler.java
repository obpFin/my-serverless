package peltonen.oskari.photoapp.users;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import peltonen.oskari.photoapp.users.service.CognitoUserService;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

import java.util.HashMap;
import java.util.Map;

public class GetUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final String appClientId;
    private final String appClientSecret;

    private final CognitoUserService cognitoUserService;

    public GetUserHandler() {
        cognitoUserService = new CognitoUserService(System.getenv("AWS_REGION"));
        this.appClientId = Utils.decryptKey("MY_COGNITO_POOL_APP_CLIENT_ID");
        this.appClientSecret = Utils.decryptKey("MY_COGNITO_POOL_APP_CLIENT_SECRET");
    }
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        LambdaLogger logger = context.getLogger();
        Map headers = new HashMap();
        headers.put("Content-type", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent().withHeaders(headers);

        try {
            Map<String, String> requestHeaders = input.getHeaders();
            JsonObject userDetails =  cognitoUserService.getUser(requestHeaders.get("AccessToken"));
            response.withBody(new Gson().toJson(userDetails, JsonObject.class));
            response.withStatusCode(200);
        } catch (AwsServiceException e) {
            logger.log(e.awsErrorDetails().errorMessage());
            ErrorResponse errorResponse = new ErrorResponse("Error: " + e.awsErrorDetails().errorMessage());
            String errorResponseJsonString = new Gson().toJson(errorResponse, ErrorResponse.class);
            response.withBody(errorResponseJsonString);
            response.withBody(e.awsErrorDetails().errorMessage());
            response.withStatusCode(e.awsErrorDetails().sdkHttpResponse().statusCode());
        } catch (Exception e) {
            logger.log(e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse("Error: " + e.getMessage());
            String errorResponseJsonString = new Gson().toJson(errorResponse, ErrorResponse.class);
            response.withBody(errorResponseJsonString);
            response.withStatusCode(500);
        }

        return response;
    }
}
