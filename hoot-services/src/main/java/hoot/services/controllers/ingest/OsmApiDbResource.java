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
 * @copyright Copyright (C) 2015 DigitalGlobe (http://www.digitalglobe.com/)
 */
package hoot.services.controllers.ingest;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hoot.services.controllers.job.JobControllerBase;
import hoot.services.utils.ResourceErrorHandler;

@Path("/ingest/apidb")
public class OsmApiDbResource extends JobControllerBase 
{
	private static final Logger log = LoggerFactory.getLogger(OsmApiDbResource.class);

	public OsmApiDbResource()
	{
		if (processScriptName ==  null)
		{
			processScriptName = "IngestFromOsmApiDbToHootServicesDb";
		}
	}
	
	@POST
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  public Response exportByBoundingBox(
    @QueryParam("inputDbUrl")
    String inputDbUrl,
    @QueryParam("bbox")
    String bbox,
    @QueryParam("outputLayerName")
    String outputLayerName)
    throws Exception
  {
		String jobId = UUID.randomUUID().toString();
		try
		{
			JSONArray jobArgs = new JSONArray();
			JSONObject param = new JSONObject();
			param.put("inputDbUrl", inputDbUrl);
			param.put("bbox", bbox);
			param.put("outputLayerName", "outputLayerName");
			JSONArray commandArgs = parseParams(param.toJSONString());
			JSONObject command = _createMakeScriptJobReq(commandArgs);
			jobArgs.add(command);

			postChainJobRquest(jobId,  jobArgs.toJSONString());
		}
		catch (Exception ex)
		{
		  ResourceErrorHandler.handleError(
			"Error running osm api db ingest job: " + ex.toString(),
		    Status.INTERNAL_SERVER_ERROR,
			log);
		}
		JSONObject res = new JSONObject();
		res.put("jobid", jobId);
		
		return Response.ok(res.toJSONString(), MediaType.APPLICATION_JSON).build();
  }
}
