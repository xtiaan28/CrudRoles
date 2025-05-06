package com.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.azure.messaging.eventgrid.*;

import java.util.Optional;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.google.gson.JsonObject;

public class FunctionDeleteRoleEvent {

    @FunctionName("deleteRole")
    public HttpResponseMessage run(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.DELETE},
            route = "roles/{id}",
            authLevel = AuthorizationLevel.FUNCTION)
        HttpRequestMessage<Optional<String>> request,
        @BindingName("id") String roleId,
        final ExecutionContext context) {

        String topicEndpoint = "https://g14-eventgrid.eastus2-1.eventgrid.azure.net/api/events";
        String topicKey = "EejWPw5R0PpJfFaPr5q6Q9O5DBU9Y4zXCNFeTOdQn3FOuebISyUyJQQJ99BEACHYHv6XJ3w3AAABAZEGXdq1";

        try {
            EventGridPublisherClient<EventGridEvent> client = new EventGridPublisherClientBuilder()
                .endpoint(topicEndpoint)
                .credential(new AzureKeyCredential(topicKey))
                .buildEventGridEventPublisherClient();

            JsonObject data = new JsonObject();
            data.addProperty("roleId", roleId);

            EventGridEvent event = new EventGridEvent(
                "CrudRoles/roles",
                "Role.Deleted",
                BinaryData.fromObject(data),
                "1.0"
            );

            client.sendEvent(event);

            return request.createResponseBuilder(HttpStatus.OK)
                .body("Evento de eliminación enviado para el rol: " + roleId)
                .build();

        } catch (Exception e) {
            context.getLogger().severe("Error enviando evento: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error enviando evento: " + e.getMessage())
                .build();
        }
    }
}
