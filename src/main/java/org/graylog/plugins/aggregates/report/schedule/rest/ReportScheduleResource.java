package org.graylog.plugins.aggregates.report.schedule.rest;

import com.codahale.metrics.annotation.Timed;
import com.mongodb.MongoException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.graylog.plugins.aggregates.permissions.ReportScheduleRestPermissions;
import org.graylog.plugins.aggregates.report.schedule.ReportSchedule;
import org.graylog.plugins.aggregates.report.schedule.ReportScheduleService;
import org.graylog.plugins.aggregates.report.schedule.rest.models.requests.AddReportScheduleRequest;
import org.graylog.plugins.aggregates.report.schedule.rest.models.requests.UpdateReportScheduleRequest;
import org.graylog.plugins.aggregates.report.schedule.rest.models.responses.ReportSchedulesList;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.shared.rest.resources.RestResource;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Api(value = "Aggregates/Schedules", description = "Management of Aggregation rules.")
@Path("/schedules")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ReportScheduleResource extends RestResource implements PluginRestResource {
    private final ReportScheduleService reportScheduleService;
    
    @Inject
    public ReportScheduleResource(ReportScheduleService ruleService) {
        this.reportScheduleService = ruleService;
    }

    @GET
    @Timed
    @ApiOperation(value = "Lists all existing report schedules")
    @RequiresAuthentication
    @RequiresPermissions(ReportScheduleRestPermissions.AGGREGATE_REPORT_SCHEDULES_READ)
    public ReportSchedulesList list() {
        final List<ReportSchedule> reportSchedules = reportScheduleService.all();   
        return ReportSchedulesList.create(reportSchedules);
    }
    
    @PUT
    @Timed    
    @ApiOperation(value = "Create a report schedule")
    @RequiresAuthentication
    @RequiresPermissions(ReportScheduleRestPermissions.AGGREGATE_REPORT_SCHEDULES_CREATE)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "The supplied request is not valid.")
    })
    public Response create(
                             @ApiParam(name = "JSON body", required = true) @Valid @NotNull AddReportScheduleRequest request
                             ) {
        final ReportSchedule reportSchedule = reportScheduleService.fromRequest(request);

        reportScheduleService.create(reportSchedule);

        return Response.accepted().build();
    }

    @POST
    @Path("/{name}")
    @Timed
    @RequiresAuthentication
    @RequiresPermissions(ReportScheduleRestPermissions.AGGREGATE_REPORT_SCHEDULES_UPDATE)
    @ApiOperation(value = "Update a report schedule")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "The supplied request is not valid.")
    })
    public Response update(@ApiParam(name = "name", required = true)
    					   @PathParam("name") String name,
                             @ApiParam(name = "JSON body", required = true) @Valid @NotNull UpdateReportScheduleRequest request
                             ) throws UnsupportedEncodingException {
        final ReportSchedule reportSchedule = reportScheduleService.fromRequest(request);

        reportScheduleService.update(java.net.URLDecoder.decode(name, "UTF-8"), reportSchedule);

        return Response.accepted().build();
    }
    
    @DELETE
    @Path("/{id}")
    @RequiresAuthentication
    @RequiresPermissions(ReportScheduleRestPermissions.AGGREGATE_REPORT_SCHEDULES_DELETE)
    @ApiOperation(value = "Delete a report schedule")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Schedule not found."),
            @ApiResponse(code = 400, message = "Invalid ObjectId.")
    })
    public void delete(@ApiParam(name = "id", required = true)
                              @PathParam("id") String id
                              ) throws NotFoundException, MongoException, UnsupportedEncodingException {
        reportScheduleService.destroy(java.net.URLDecoder.decode(id, "UTF-8"));
    }
}