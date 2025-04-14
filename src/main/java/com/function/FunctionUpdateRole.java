package com.function;

import com.function.model.Role;
import com.function.service.RoleService;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

public class FunctionUpdateRole {

    @FunctionName("UpdateRole")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<Role>> request,
            final ExecutionContext context) {

        Logger logger = context.getLogger();
        logger.info("Iniciando función para actualizar rol");

        Optional<Role> optionalRole = request.getBody();
        if (!optionalRole.isPresent()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("El cuerpo de la solicitud está vacío o no es válido")
                    .build();
        }

        Role role = optionalRole.get();

        try {
            boolean updated = RoleService.updateRole(role);
            if (!updated) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("No se encontró el rol con ID: " + role.getRoleId())
                        .build();
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .body("Rol actualizado correctamente")
                    .build();
        } catch (SQLException e) {
            logger.severe("Error al actualizar rol: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar rol: " + e.getMessage())
                    .build();
        }
    }
}