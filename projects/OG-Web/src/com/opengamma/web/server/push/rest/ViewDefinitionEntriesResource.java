package com.opengamma.web.server.push.rest;

import com.opengamma.engine.view.ViewDefinitionRepository;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * REST interface that produces a JSON list of view definition names for populating the web client.
 */
@Path("viewdefinitions")
public class ViewDefinitionEntriesResource {

  private final ViewDefinitionRepository _viewDefinitionRepository;

  public ViewDefinitionEntriesResource(ViewDefinitionRepository viewDefinitionRepository) {
    _viewDefinitionRepository = viewDefinitionRepository;
  }

  /**
   * @return {@code {viewDefId1: viewDefName1, viewDefId2: viewDefName2, ...}}
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getViewDefinitionEntriesJson() {
    return new JSONObject(_viewDefinitionRepository.getDefinitionEntries()).toString();
  }
}
