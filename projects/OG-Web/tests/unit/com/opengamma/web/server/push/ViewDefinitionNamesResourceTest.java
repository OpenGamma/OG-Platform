package com.opengamma.web.server.push;

import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.util.tuple.Pair;
import org.eclipse.jetty.server.Server;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.opengamma.web.server.push.WebPushTestUtils.readFromPath;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class ViewDefinitionNamesResourceTest {

  @Test
  public void getViewDefinitionNamesJson() throws JSONException {
    ViewDefinitionRepository repository = mock(ViewDefinitionRepository.class);
    Set<String> viewDefNames = new HashSet<String>();
    viewDefNames.add("viewDef1");
    viewDefNames.add("viewDef2");
    when(repository.getDefinitionNames()).thenReturn(viewDefNames);
    ViewDefinitionNamesResource resource = new ViewDefinitionNamesResource(repository);
    JSONObject jsonObject = new JSONObject(resource.getViewDefinitionNamesJson());
    JSONArray jsonArray = jsonObject.getJSONArray(ViewDefinitionNamesResource.VIEW_DEFINITION_NAMES);
    assertEquals(2, jsonArray.length());
    List<String> names = Arrays.asList(jsonArray.getString(0), jsonArray.getString(1));
    assertTrue(names.contains("viewDef1"));
    assertTrue(names.contains("viewDef2"));
  }

  @Test
  public void getViewDefinitionNamesOverHttp() throws Exception {
    Pair<Server, WebApplicationContext> serverAndContext =
        WebPushTestUtils.createJettyServer("classpath:/com/opengamma/web/viewdefinitionnamesresource-test.xml");
    Server server = serverAndContext.getFirst();
    JSONObject json = new JSONObject(WebPushTestUtils.readFromPath("/jax/viewdefinitionnames"));
    JSONArray namesArray = json.getJSONArray(ViewDefinitionNamesResource.VIEW_DEFINITION_NAMES);
    assertEquals(2, namesArray.length());
    List<String> names = Arrays.asList(namesArray.getString(0), namesArray.getString(1));
    assertTrue(names.contains("viewDef1"));
    assertTrue(names.contains("viewDef2"));
    server.stop();
  }
}
