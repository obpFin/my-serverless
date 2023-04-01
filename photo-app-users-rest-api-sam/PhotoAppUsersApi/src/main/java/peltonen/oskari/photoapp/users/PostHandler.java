package peltonen.oskari.photoapp.users;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;

/**
 * Handler for requests to Lambda function.
 */
public class PostHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {

        LambdaLogger logger = context.getLogger();
        logger.log("Handling HTTP POST request for the /users endpoint");

        String requestBody = input.getBody();

        Gson gson = new Gson();
        Map<String, String> userDetails = gson.fromJson(requestBody, Map.class);
        userDetails.put("userId", UUID.randomUUID().toString());

        //TODO: Process user details

        Map returnValue = new HashMap();
        returnValue.put("firstName", userDetails.get("firstName"));
        returnValue.put("lastName", userDetails.get("lastName"));
        returnValue.put("userId", userDetails.get("userId"));

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.withStatusCode(200).withBody(gson.toJson(returnValue, Map.class));

        Map responseHeaders = new HashMap();
        responseHeaders.put("Content-type", "application/json");
        response.withHeaders(responseHeaders);

        return response;
    }
}
