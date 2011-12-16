package com.opengamma.web.server.push.rest;

import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.conversion.ConversionMode;
import com.opengamma.web.server.push.Viewport;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST resource that wraps a {@link Viewport}.
 */
public class ViewportResource {

  private final Viewport _viewport;

  public ViewportResource(Viewport viewport) {
    _viewport = viewport;
  }

  /**
   * @return JSON containing the structure of the viewport's portfolio and primitiave grids.  This doesn't include
   * the structure of any dependency graph grids
   */
  @Path("grid")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getGridStructure() {
    return new JSONObject(_viewport.getGridStructure()).toString();
  }

  /**
   * @return JSON containing the viewport's latest data
   */
  @Path("data")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getLatestData() {
    return new JSONObject(_viewport.getLatestResults()).toString();
  }

  /**
   * Pauses or unpauses the view.
   * @param run {@code true} to run the view, {@code false} to pause it
   * @return An empty response
   */
  // TODO is a query param the right way to do this? easy to implement but is that adequate justification?
  @POST
  @Path("running")
  public Response setRunning(@QueryParam("run") boolean run) {
    _viewport.setRunning(run);
    return Response.ok().build();
  }

  //-----------------------
  // split these into a GridResource? don't really want to expose the grids. what would a grid interface need?
  // getCsv
  // setConversionMode(row, col)
  // Viewport would need getPortfolioGrid, getPrimitivesGrid, getPortfolioDependencyGraphGrid, getPrimitivesDependencyGraphGrid

  /**
   * @return The portfolio grid data as CSV.
   */
  @Path("portfolio")
  @GET
  @Produces("text/csv")
  public Response getPortfolioCsv() {
    return buildCsvResponse(_viewport.getPortfolioCsv());
  }

  /**
   * Returns a portfolio grid cell's dependency graph data as CSV.
   * @param row The row of the cell whose dependency graph is required
   * @param col The column of the cell whose dependency graph is required
   * @return Response containing a CSV file.  If the specified cell doesn't have a dependency graph a response with
   * status 404 is returned
   */
  @Path("portfolio/{row}/{col}")
  @GET
  @Produces("text/csv")
  public Response getPortfolioDependencyGraphCsv(@PathParam("row") int row, @PathParam("col") int col) {
    return buildCsvResponse(_viewport.getPortfolioCsv(row, col));
  }

  /**
   * @return The primitive grid data as CSV.
   */
  @Path("primitives")
  @GET
  @Produces("text/csv")
  public Response getPrimitivesCsv() {
    return buildCsvResponse(_viewport.getPrimitivesCsv());
  }

  /**
   * Returns a primitive grid cell's dependency graph data as CSV.
   * @param row The row of the cell whose dependency graph is required
   * @param col The column of the cell whose dependency graph is required
   * @return Response containing a CSV file.  If the specified cell doesn't have a dependency graph a response with
   * status 404 is returned
   */
  @Path("primitives/{row}/{col}")
  @GET
  @Produces("text/csv")
  public Response getPrimitivesDependencyGraphCsv(@PathParam("row") int row, @PathParam("col") int col) {
    return buildCsvResponse(_viewport.getPrimitivesCsv(row, col));
  }

  private static Response buildCsvResponse(Pair<String, String> fileNameAndCsv) {
    if (fileNameAndCsv == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    } else {
      String filename = fileNameAndCsv.getFirst();
      String csv = fileNameAndCsv.getSecond();
      return Response.ok(csv).header("Content-Disposition", "attachment; filename=" + filename).build();
    }
  }
}
