/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import static com.opengamma.web.WebResourceTestUtils.assertJSONObjectEquals;
import static com.opengamma.web.WebResourceTestUtils.loadJson;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Collections;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link WebPositionsResource}.
 */
@Test(groups = TestGroup.UNIT)
public class WebPositionsResourceTest extends AbstractWebPositionResourceTestCase {
  
  @Test
  public void testAddPositionWithTrades() throws Exception {
    String tradesJson = getTradesJson();
    Response response = _webPositionsResource.postJSON("10", SEC_ID.getScheme().getName(), SEC_ID.getValue(), tradesJson, null, null, null);
    assertNotNull(response);
    assertEquals(201, response.getStatus());
    assertEquals("/positions/MemPos~1", getActualURL(response));
    assertPositionAndTrades();
  }
  
  @Test
  public void testAddPositionWithEmptyTrades() throws Exception {
    String tradesJson = EMPTY_TRADES;
    Response response = _webPositionsResource.postJSON("10", SEC_ID.getScheme().getName(), SEC_ID.getValue(), tradesJson, null, null, null);
    assertNotNull(response);
    assertEquals(201, response.getStatus());
    assertEquals("/positions/MemPos~1", getActualURL(response));
    assertPositionWithNoTrades();
  }

  @Test
  public void testAddPositionWithNoTrades() throws Exception {
    Response response = _webPositionsResource.postJSON("10", SEC_ID.getScheme().getName(), SEC_ID.getValue(), null, null, null, null);
    assertNotNull(response);
    assertEquals(201, response.getStatus());
    assertEquals("/positions/MemPos~1", getActualURL(response));
    assertPositionWithNoTrades();
  }
  
  @Test
  public void testGetAllPositions() throws Exception {
    populatePositionMaster();
    MultivaluedMap<String, String> queryParameters = _uriInfo.getQueryParameters();
    queryParameters.putSingle("identifier", StringUtils.EMPTY);
    queryParameters.putSingle("minquantity", StringUtils.EMPTY);
    queryParameters.putSingle("maxquantity", StringUtils.EMPTY);
    queryParameters.put("tradeId", Collections.<String>emptyList());
    queryParameters.put("positionId", Collections.<String>emptyList());
    
    String allPositions = _webPositionsResource.getJSON(null, null, null, null, null, null, queryParameters.get("positionId"), queryParameters.get("tradeId"), null);
    assertNotNull(allPositions);
    assertJSONObjectEquals(loadJson("com/opengamma/web/position/allPositionsJson.txt"), new JSONObject(allPositions));
  }
}
