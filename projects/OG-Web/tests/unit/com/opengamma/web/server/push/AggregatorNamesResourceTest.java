/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.google.common.collect.ImmutableSet;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.AggregatedViewDefinitionManager;
import com.opengamma.web.server.push.rest.AggregatorNamesResource;
import org.eclipse.jetty.server.Server;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

public class AggregatorNamesResourceTest {

  private static final String AGGREGATOR1 = "aggregator1";
  private static final String AGGREGATOR2 = "aggregator2";

  @Test
  public void getAggregatorNamesJson() throws JSONException {
    AggregatedViewDefinitionManager viewDefinitionManager = mock(AggregatedViewDefinitionManager.class);
    when(viewDefinitionManager.getAggregatorNames()).thenReturn(ImmutableSet.of(AGGREGATOR1, AGGREGATOR2));
    AggregatorNamesResource resource = new AggregatorNamesResource(viewDefinitionManager);
    JSONArray json = new JSONArray(resource.getAggregatorNamesJson());
    assertEquals(2, json.length());
    assertEquals(AGGREGATOR1, json.get(0));
    assertEquals(AGGREGATOR2, json.get(1));
  }

  @Test
  public void getAggregatorNamesOverHttp() throws Exception {
    Pair<Server, WebApplicationContext> serverAndContext =
        WebPushTestUtils.createJettyServer("classpath:/com/opengamma/web/server/push/aggregatornamesresource-test.xml");
    Server server = serverAndContext.getFirst();
    JSONArray json = new JSONArray(WebPushTestUtils.readFromPath("/jax/aggregatornames"));
    assertEquals(2, json.length());
    assertEquals(AGGREGATOR1, json.get(0));
    assertEquals(AGGREGATOR2, json.get(1));
    server.stop();
  }
  
  public static class AggregatedViewDefinitionManagerFactoryBean extends AbstractFactoryBean<AggregatedViewDefinitionManager> {

    @Override
    public Class<AggregatedViewDefinitionManager> getObjectType() {
      return AggregatedViewDefinitionManager.class;
    }

    @Override
    protected AggregatedViewDefinitionManager createInstance() throws Exception {
      AggregatedViewDefinitionManager viewDefinitionManager = mock(AggregatedViewDefinitionManager.class);
      when(viewDefinitionManager.getAggregatorNames()).thenReturn(ImmutableSet.of(AGGREGATOR1, AGGREGATOR2));
      return viewDefinitionManager;
    }
  }

}
