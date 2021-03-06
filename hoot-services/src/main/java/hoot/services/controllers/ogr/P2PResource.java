/*
 * This file is part of Hootenanny.
 *
 * Hootenanny is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * --------------------------------------------------------------------
 *
 * The following copyright notices are generated automatically. If you
 * have a new notice to add, please use the format:
 * " * @copyright Copyright ..."
 * This will properly maintain the copyright information. DigitalGlobe
 * copyrights will be updated automatically.
 *
 * @copyright Copyright (C) 2016 DigitalGlobe (http://www.digitalglobe.com/)
 */
package hoot.services.controllers.ogr;

import static hoot.services.HootProperties.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hoot.services.nodejs.ServerControllerBase;


public class P2PResource extends ServerControllerBase {
    private static final Logger logger = LoggerFactory.getLogger(P2PResource.class);

    private static Process p2PServiceProcess;

    public P2PResource() {}

    public static void startP2PService() {
        try {
            String p2PServiceScript = HOME_FOLDER + "/scripts/" + P_2_P_SERVER_SCRIPT;

            // Make sure to wipe out previosuly running servers.
            stopServer(p2PServiceScript);

            p2PServiceProcess = startServer(P_2_P_SERVER_PORT, P_2_P_SERVER_THREAD_COUNT, p2PServiceScript);
        }
        catch (Exception e) {
            String msg = "Error starting Point-To-Polygon Service: " + e.getMessage();
            throw new RuntimeException(msg, e);
        }
    }

    public static void stopP2PService() {
        // This also gets called automatically from HootServletContext when
        // service exits but should not be reliable since there are many path where it will not be invoked.
        try {
            // Destroy the reference to the process directly here via the Java
            // API vs having the base class kill it with a unix command. Killing it via command causes
            // the stxxl temp files created by hoot threads not to be cleaned up.
            // stopServer(homeFolder + "/scripts/" + translationServerScript);
            p2PServiceProcess.destroy();
        }
        catch (Exception e) {
            String msg = "Error stopping Translation Service: " + e.getMessage();
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Destroys all POI to POI server process where it effectively shutting them
     * down.
     * 
     * GET hoot-services/services/p2pserver/stop
     * 
     * @return JSON containing state
     */
    @GET
    @Path("/p2pserver/stop")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopP2PServiceViaWeb() {
        // This also gets called automatically from HootServletContext when
        // service exits but should not be reliable since there are many path where it will not be invoked.
        try {
            // Destroy the reference to the process directly here via the Java
            // API vs having the base class kill it with a unix command. Killing it via command causes
            // the stxxl temp files created hoot threads not to be cleaned up.
            stopP2PService();
        }
        catch (Exception e) {
            String msg = "Error stopping Point-To-Polygon service: " + e;
            throw new WebApplicationException(e, Response.serverError().entity(msg).build());
        }

        JSONObject json = new JSONObject();
        json.put("isRunning", "false");

        return Response.ok(json.toJSONString()).build();
    }

    /**
     * Gets current status of P2P server.
     * 
     * GET hoot-services/services/p2pserver/status
     * 
     * @return JSON containing state and port it is running
     */
    @GET
    @Path("/p2pserver/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isP2PServiceRunning() {
        boolean isRunning;

        try {
            isRunning = getStatus(p2PServiceProcess);
        }
        catch (Exception e) {
            String msg = "Error getting status of Point-To-Polygon Service: " + e.getMessage();
            throw new WebApplicationException(e, Response.serverError().entity(msg).build());
        }

        JSONObject json = new JSONObject();
        json.put("isRunning", isRunning);
        json.put("port", P_2_P_SERVER_PORT);

        return Response.ok(json.toJSONString()).build();
    }
}
