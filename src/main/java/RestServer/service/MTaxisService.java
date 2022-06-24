package RestServer.service;

import RestServer.beans.CoordMTaxiList;
import RestServer.beans.MTaxi;
import RestServer.beans.MTaxis;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

@Path("mtaxis")
public class MTaxisService {
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getMTaxis(){
        return Response.ok(MTaxis.getInstance()).build();

    }

    @Path("add")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response addMTaxi(MTaxi u){
        CoordMTaxiList result = MTaxis.getInstance().add(u);
        if(result != null)
            return Response.ok(result).build();
        else
            return Response.status(Response.Status.CONFLICT).build();
    }

    @Path("get")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getMTaxisList(){
        ArrayList<MTaxi> l = MTaxis.getInstance().getmTaxisList();
        return Response.ok(l).build();
    }

    @Path("get/{id}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getById(@PathParam("id") int id){
        MTaxi u = MTaxis.getInstance().getById(id);
        if(u!=null)
            return Response.ok(u).build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Path("remove/{id}")
    @DELETE
    @Produces({"application/json", "application/xml"})
    public Response deleteById(@PathParam("id") int id){
        MTaxi u = MTaxis.getInstance().deleteById(id);
        if(u!=null)
            return Response.ok(u).build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }
}
