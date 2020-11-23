package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dtos.UserDTO;
import entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.*;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import facades.UserFacade;
import utils.APIFetcher;
import utils.EMF_Creator;

/**
 * @author lam@cphbusiness.dk
 */
@Path("info")
public class DemoResource {
    
    private static final EntityManagerFactory EMF = EMF_Creator.createEntityManagerFactory();


    private static final UserFacade FACADE = UserFacade.getUserFacade(EMF);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Context
    private UriInfo context;

    @Context
    SecurityContext securityContext;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getInfoForAll() {
        return "{\"msg\":\"Hello anonymous\"}";
    }

    //Just to verify if the database is setup
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("all")
    public String allUsers() {

        EntityManager em = EMF.createEntityManager();
        try {
            TypedQuery<User> query = em.createQuery ("select u from User u",entities.User.class);
            List<User> users = query.getResultList();
            return "[" + users.size() + "]";
        } finally {
            em.close();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("user")
    @RolesAllowed("user")
    public String getFromUser() {
        String thisuser = securityContext.getUserPrincipal().getName();
        return "{\"msg\": \"Hello to User: " + thisuser + "\"}";
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("userjson/{username}")
    @RolesAllowed("user")
    public Response getUserJson(@PathParam("username") String username) {
        UserDTO user = FACADE.getUserDTO(username);
        return Response.ok()
                .entity(GSON.toJson(user))
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("admin")
    @RolesAllowed("admin")
    public String getFromAdmin() {
        String thisuser = securityContext.getUserPrincipal().getName();
        return "{\"msg\": \"Hello to (admin) User: " + thisuser + "\"}";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("fetchdemo")
    public String fetchDemo() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<APIFetcher> apifetchers = new ArrayList<>();
        List<Future<String>> futures = new ArrayList<>();
        List<String> results = new ArrayList<>();

        apifetchers.add(new APIFetcher("https://swapi.dev/api/people/"));
        apifetchers.add(new APIFetcher("https://www.foaas.com/asshole/regards%20group%2011"));
        apifetchers.add(new APIFetcher("https://baconipsum.com/api/?type=meat-and-filler"));
        apifetchers.add(new APIFetcher("https://some-random-api.ml/facts/cat"));
        apifetchers.add(new APIFetcher("https://some-random-api.ml/facts/panda"));

        for (APIFetcher fetcher : apifetchers) {
            Future<String> future = executor.submit(fetcher);
            futures.add(future);
        }

        for (Future<String> future : futures) {
            results.add(future.get());
        }

        return new Gson().toJson(results);
    }
}