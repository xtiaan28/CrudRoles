package com.function;

import com.google.gson.*;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.sql.SQLException;
import java.util.logging.Logger;

import com.function.service.RoleService;

public class FunctionProcessRoleDeletion {

    @FunctionName("ProcessRoleDeletionEvent")
    public void run(
            @EventGridTrigger(name = "eventGridEvent") String content,
            final ExecutionContext context) {

        Logger logger = context.getLogger();
        logger.info("Evento de eliminación de rol recibido.");
        logger.info("Contenido recibido: " + content);

        try {
            Gson gson = new Gson();
            JsonObject eventGridEvent = gson.fromJson(content, JsonObject.class);

            String eventType = eventGridEvent.get("eventType").getAsString();
            JsonObject data = eventGridEvent.getAsJsonObject("data");

            // Nueva navegación hacia roleId
            JsonObject members = data.getAsJsonObject("members");
            JsonObject roleIdObject = members.getAsJsonObject("roleId");
            int roleId = Integer.parseInt(roleIdObject.get("value").getAsString());

            logger.info("Tipo de evento: " + eventType);
            logger.info("ID del rol a eliminar: " + roleId);

            boolean deleted = RoleService.deleteRole(roleId);
            if (deleted) {
                logger.info("Rol eliminado correctamente.");
            } else {
                logger.warning("No se encontró el rol con ID: " + roleId);
            }

        } catch (Exception e) {
            logger.severe("Error al procesar evento de eliminación: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
