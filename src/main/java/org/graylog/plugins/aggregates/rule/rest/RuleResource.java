package org.graylog.plugins.aggregates.rule.rest;

import com.codahale.metrics.annotation.Timed;
import com.mongodb.MongoException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.graylog.plugins.aggregates.permissions.RuleRestPermissions;
import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.rule.RuleService;
import org.graylog.plugins.aggregates.rule.rest.models.requests.AddRuleRequest;
import org.graylog.plugins.aggregates.rule.rest.models.requests.UpdateRuleRequest;
import org.graylog.plugins.aggregates.rule.rest.models.responses.RulesList;
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

@Api(value = "Aggregates", description = "Management of Aggregation rules.")
@Path("/rules")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RuleResource extends RestResource implements PluginRestResource {
    private final RuleService ruleService;  
    
    @Inject
    public RuleResource(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GET
    @Timed
    @ApiOperation(value = "Lists all existing rules")
    @RequiresAuthentication
    @RequiresPermissions(RuleRestPermissions.AGGREGATE_RULES_READ)
    public RulesList list() {
        final List<Rule> rules = ruleService.all();   
        return RulesList.create(rules);
    }
    
    @PUT
    @Timed    
    @ApiOperation(value = "Create a rule")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "The supplied request is not valid.")
    })
    public Response create(
                             @ApiParam(name = "JSON body", required = true) @Valid @NotNull AddRuleRequest request
                             ) {
        final Rule rule = ruleService.fromRequest(request);

        ruleService.create(rule);

        return Response.accepted().build();
    }

    @POST
    @Path("/{name}")
    @Timed    
    @ApiOperation(value = "Update a rule")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "The supplied request is not valid.")
    })
    public Response update(@ApiParam(name = "name", required = true)
    					   @PathParam("name") String name,
                             @ApiParam(name = "JSON body", required = true) @Valid @NotNull UpdateRuleRequest request
                             ) throws UnsupportedEncodingException {
        final Rule rule = ruleService.fromRequest(request);

        ruleService.update(java.net.URLDecoder.decode(name, "UTF-8"), rule);

        return Response.accepted().build();
    }
    
    @DELETE
    @Path("/{name}")
    @RequiresAuthentication
    @RequiresPermissions(RuleRestPermissions.AGGREGATE_RULES_DELETE)
    @ApiOperation(value = "Delete a rule")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Rule not found."),
            @ApiResponse(code = 400, message = "Invalid ObjectId.")
    })
    public void delete(@ApiParam(name = "name", required = true)
                              @PathParam("name") String name
                              ) throws NotFoundException, MongoException, UnsupportedEncodingException {
        ruleService.destroy(java.net.URLDecoder.decode(name, "UTF-8"));
    }
}