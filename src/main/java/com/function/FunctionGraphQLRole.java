package com.function;

import com.function.provider.GraphQLProvider;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.Map;

public class FunctionGraphQLRole {
    @FunctionName("GraphQLRoles")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "graphql"
            )
            HttpRequestMessage<Optional<Map<String, Object>>> request,
            final ExecutionContext context) {

        Logger logger = context.getLogger();
        logger.info("Procesando solicitud GraphQL para Roles");

        try {
            Map<String, Object> body = request.getBody().orElse(null);
            if (body == null || !body.containsKey("query")) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Se esperaba un campo 'query' en el body")
                        .build();
            }

            String query = (String) body.get("query");

            GraphQL graphQL = GraphQLProvider.getGraphQL();
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                    .query(query)
                    .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(executionResult.toSpecification())
                    .build();

        } catch (Exception e) {
            logger.severe("Error ejecutando GraphQL: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error ejecutando GraphQL: " + e.getMessage())
                    .build();
        }
    }
}