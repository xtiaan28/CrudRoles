package com.function;

import com.function.model.Role;
import com.function.service.RoleService;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

public class FunctionGetRoleById {

    @FunctionName("GetRoleById")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                route = "roles/{id}",
                authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") int id,
            final ExecutionContext context) {

        Logger logger = context.getLogger();
        logger.info("Iniciando función para obtener rol con ID: " + id);

        try {
            Role role = RoleService.getRoleById(id);

            if (role == null) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("Rol no encontrado con ID: " + id)
                        .build();
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(role)
                    .build();
        } catch (SQLException e) {
            logger.severe("Error al obtener rol: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener rol: " + e.getMessage())
                    .build();
        }
    }
}