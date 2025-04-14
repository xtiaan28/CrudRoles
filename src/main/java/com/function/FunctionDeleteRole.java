package com.function;

import com.function.service.RoleService;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

public class FunctionDeleteRole {

    @FunctionName("DeleteRole")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.DELETE},
                route = "roles/{id}",
                authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") int id,
            final ExecutionContext context) {

        Logger logger = context.getLogger();
        logger.info("Iniciando función para eliminar rol con ID: " + id);

        try {
            boolean deleted = RoleService.deleteRole(id);
            if (!deleted) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("No se encontró el rol con ID: " + id)
                        .build();
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .body("Rol eliminado correctamente")
                    .build();
        } catch (SQLException e) {
            logger.severe("Error al eliminar rol: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar rol: " + e.getMessage())
                    .build();
        }
    }
}