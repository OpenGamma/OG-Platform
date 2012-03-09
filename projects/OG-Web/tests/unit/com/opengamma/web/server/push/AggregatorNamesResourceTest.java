/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import static org.testng.AssertJUnit.assertEquals;

import org.eclipse.jetty.server.Server;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.push.rest.AggregatorNamesResource;

public class AggregatorNamesResourceTest {

  private static final String AGGREGATOR1 = "aggregator1";
  private static final String AGGREGATOR2 = "aggregator2";

  @Test
  public void getAggregatorNamesJson() throws JSONException {
    AggregatorNamesResource resource = new AggregatorNamesResource(ImmutableSet.of(AGGREGATOR1, AGGREGATOR2));
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
    JSONArray json = new JSONArray(WebPushTestUtils.readFromPath("/jax/aggregators"));
    assertEquals(2, json.length());
    assertEquals(AGGREGATOR1, json.get(0));
    assertEquals(AGGREGATOR2, json.get(1));
    server.stop();
  }

}
