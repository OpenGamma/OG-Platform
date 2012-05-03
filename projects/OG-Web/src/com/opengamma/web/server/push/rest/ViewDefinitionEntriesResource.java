package com.opengamma.web.server.push.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.id.UniqueId;

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
   * @return {@code [{id: viewDefId1, name: viewDefName1}, {id: viewDefId2, name: viewDefName2}, ...]}
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getViewDefinitionEntriesJson() {
    Map<UniqueId, String> viewDefs = _viewDefinitionRepository.getDefinitionEntries();
    List<Map<String, Object>> viewDefList = new ArrayList<Map<String, Object>>(viewDefs.size());
    for (Map.Entry<UniqueId, String> entry : viewDefs.entrySet()) {
      UniqueId id = entry.getKey();
      String name = entry.getValue();
      viewDefList.add(ImmutableMap.<String, Object>of("id", id, "name", name));
    }
    return new JSONArray(viewDefList).toString();
  }
}
