package com.function.provider;

import com.function.model.Role;
import com.function.service.RoleService;
import graphql.schema.*;
import graphql.GraphQL;
import graphql.schema.idl.*;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class GraphQLProvider {
    private static GraphQL graphQL;

    public static GraphQL getGraphQL() throws Exception {
        if (graphQL == null) {
            InputStream schemaInputStream = GraphQLProvider.class.getResourceAsStream("/schema.graphqls");
            TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schemaInputStream);

            RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", builder -> builder.dataFetcher("getAllRoles", getRolesFetcher()))
                .type("Mutation", builder -> builder.dataFetcher("createRole", createRoleFetcher()))
                .build();

            SchemaGenerator schemaGenerator = new SchemaGenerator();
            GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, wiring);
            graphQL = GraphQL.newGraphQL(graphQLSchema).build();
        }
        return graphQL;
    }

    private static DataFetcher<List<Role>> getRolesFetcher() {
        return dataFetchingEnvironment -> RoleService.getAllRoles();
    }

    private static DataFetcher<Role> createRoleFetcher() {
        return dataFetchingEnvironment -> {
            String name = dataFetchingEnvironment.getArgument("name");
            String description = dataFetchingEnvironment.getArgument("description");

            Role role = new Role();
            role.setName(name);
            role.setDescription(description);

            return RoleService.createRole(role);
        };
    }
}