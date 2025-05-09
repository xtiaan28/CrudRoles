package com.function;

import com.google.gson.*;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import com.function.model.User;
import com.function.service.RoleService;

public class FunctionProcessRoleDeletion {

    private static final int DEFAULT_ROLE_ID = 2;

    @FunctionName("ProcessRoleDeletionEvent")
    public void run(
            @EventGridTrigger(name = "eventGridEvent") String content,
            final ExecutionContext context) {

        Logger logger = context.getLogger();
        logger.info("Evento de eliminación de rol recibido.");

        try {
            Gson gson = new Gson();
            JsonObject eventGridEvent = gson.fromJson(content, JsonObject.class);

            String eventType = eventGridEvent.get("eventType").getAsString();
            JsonObject data = eventGridEvent.getAsJsonObject("data");

            // Nueva navegación hacia roleId
            JsonObject members = data.getAsJsonObject("members");
            JsonObject roleIdObject = members.getAsJsonObject("roleId");
            int roleIdToDelete = Integer.parseInt(roleIdObject.get("value").getAsString());
            logger.info("Tipo de evento: " + eventType);
            logger.info("Procesando eliminación del rol ID: " + roleIdToDelete);

            // 2. Obtener usuarios con este rol
            logger.info("Buscando usuarios con el rol " + roleIdToDelete + "...");
            List<User> users = RoleService.getUsersByRole(roleIdToDelete);
            logger.info("Encontrados " + users.size() + " usuarios");

            if (!users.isEmpty()) {
                logger.info("Actualizando usuarios al rol por defecto (" + DEFAULT_ROLE_ID + ")...");
                int updated = RoleService.bulkUpdateUserRole(users, DEFAULT_ROLE_ID);
                logger.info(updated + " usuarios actualizados");
            }

            logger.info("Eliminando rol...");
            boolean deleted = RoleService.deleteRole(roleIdToDelete);

            if (deleted) {
                logger.info("########## OPERACIÓN EXITOSA ##########");
                logger.info("Rol " + roleIdToDelete + " eliminado");
                logger.info(users.size() + " usuarios migrados a rol " + DEFAULT_ROLE_ID);
            } else {
                logger.warning("No se pudo eliminar el rol " + roleIdToDelete);
            }

        } catch (Exception e) {
            logger.severe("Error al procesar evento de eliminación: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
