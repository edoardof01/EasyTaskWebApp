package orm;

import domain.Feed;
import domain.Task;
import domain.Topic;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/feed")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FeedEndpoint {

    private final Feed feed = Feed.getInstance();

    @GET
    @Path("/filtered")
    public Response getFilteredFeed() {
        return Response.ok(feed.getFilteredFeed()).build();
    }

    @POST
    @Path("/filters")
    public Response setFilters(
            @QueryParam("topic") Topic topic,
            @QueryParam("groupFilter") boolean groupFilter,
            @QueryParam("sharedFilter") boolean sharedFilter) {
        feed.setTopicFilter(topic);
        feed.setGroupFilter(groupFilter);
        feed.setSharedFilter(sharedFilter);
        return Response.ok().build();
    }

}

