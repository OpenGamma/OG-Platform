package com.opengamma.web.server.push;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.Test;

import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.push.rest.ViewDefinitionEntriesResource;

public class ViewDefinitionEntriesResourceTest {

  @Test
  public void getViewDefinitionNamesJson() throws JSONException {
    ViewDefinitionRepository repository = mock(ViewDefinitionRepository.class);
    Map<UniqueId, String> viewDefs = new HashMap<UniqueId, String>();
    viewDefs.put(UniqueId.of("TST", "vd1"), "viewDef1");
    when(repository.getDefinitionEntries()).thenReturn(viewDefs);
    ViewDefinitionEntriesResource resource = new ViewDefinitionEntriesResource(repository);
    JSONArray json = new JSONArray(resource.getViewDefinitionEntriesJson());
    assertEquals(1, json.length());

    JSONObject def1 = json.getJSONObject(0);
    assertEquals("viewDef1", def1.get("name"));
    assertEquals(UniqueId.of("TST", "vd1").toString(), def1.get("id"));
  }

  @Test
  public void getViewDefinitionNamesOverHttp() throws Exception {
    Pair<Server, WebApplicationContext> serverAndContext =
        WebPushTestUtils.createJettyServer("classpath:/com/opengamma/web/server/push/viewdefinitionentriesresource-test.xml");
    Server server = serverAndContext.getFirst();
    JSONArray json = new JSONArray(WebPushTestUtils.readFromPath("/jax/viewdefinitions"));
    assertEquals(1, json.length());

    JSONObject def1 = json.getJSONObject(0);
    assertEquals("viewDef1", def1.get("name"));
    assertEquals(UniqueId.of("TST", "vd1").toString(), def1.get("id"));
    server.stop();
  }
}
