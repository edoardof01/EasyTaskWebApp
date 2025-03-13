package Endpoints;

import domain.Task;
import domain.Topic;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import service.FeedService;

import java.util.List;

@Path("/feed")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FeedEndpoint {

    @Inject
    private FeedService feedService;


    @GET
    public List<Task> getFeed(@QueryParam("topic") Topic topicParam,
                              @QueryParam("groupFilter") Boolean groupFilterParam,
                              @QueryParam("sharedFilter") Boolean sharedFilterParam) {

        if (topicParam != null) {
            feedService.setTopicFilter(topicParam);
        }
        if (groupFilterParam != null) {
            feedService.setGroupFilter(groupFilterParam);
        }
        if (sharedFilterParam != null) {
            feedService.setSharedFilter(sharedFilterParam);
        }
        return feedService.getFilteredFeed();
    }



    @POST
    @Path("/resetFilters")
    public void resetFilters() {
        feedService.resetFilters();
    }
}
