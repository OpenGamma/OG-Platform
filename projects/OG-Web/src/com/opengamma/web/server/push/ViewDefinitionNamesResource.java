package com.opengamma.web.server.push;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.ViewDefinitionRepository;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * REST interface that produces a JSON list of view definition names for populating the web client.
 */
@Path("viewdefinitionnames")
public class ViewDefinitionNamesResource {

  /* package */ static final String VIEW_DEFINITION_NAMES = "viewDefinitionNames";

  private final ViewDefinitionRepository _viewDefinitionRepository;

  public ViewDefinitionNamesResource(ViewDefinitionRepository viewDefinitionRepository) {
    _viewDefinitionRepository = viewDefinitionRepository;
  }

  /**
   * @return {@code {"viewDefinitionNames": [name1, name2, ...]}}
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getViewDefinitionNamesJson() {
    Map<String, Set<String>> names = new HashMap<String, Set<String>>();
    names.put(VIEW_DEFINITION_NAMES, _viewDefinitionRepository.getDefinitionNames());
    return new JSONObject(names).toString();
  }
}
