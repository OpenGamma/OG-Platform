package com.opengamma.web.server.push;

import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.push.rest.ViewDefinitionEntriesResource;
import org.eclipse.jetty.server.Server;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

public class ViewDefinitionEntriesResourceTest {

  @Test
  public void getViewDefinitionNamesJson() throws JSONException {
    ViewDefinitionRepository repository = mock(ViewDefinitionRepository.class);
    Map<UniqueId, String> viewDefs = new HashMap<UniqueId, String>();
    viewDefs.put(UniqueId.of("TST", "vd1"), "viewDef1");
    viewDefs.put(UniqueId.of("TST", "vd2"), "viewDef2");
    when(repository.getDefinitionEntries()).thenReturn(viewDefs);
    ViewDefinitionEntriesResource resource = new ViewDefinitionEntriesResource(repository);
    JSONObject json = new JSONObject(resource.getViewDefinitionEntriesJson());
    assertEquals(2, json.length());
    assertEquals("viewDef1", json.getString("TST~vd1"));
    assertEquals("viewDef2", json.getString("TST~vd2"));
  }

  @Test
  public void getViewDefinitionNamesOverHttp() throws Exception {
    Pair<Server, WebApplicationContext> serverAndContext =
        WebPushTestUtils.createJettyServer("classpath:/com/opengamma/web/server/push/viewdefinitionentriesresource-test.xml");
    Server server = serverAndContext.getFirst();
    JSONObject json = new JSONObject(WebPushTestUtils.readFromPath("/jax/viewdefinitions"));
    assertEquals(2, json.length());
    assertEquals("viewDef1", json.getString("TST~vd1"));
    assertEquals("viewDef2", json.getString("TST~vd2"));
    server.stop();
  }
}
