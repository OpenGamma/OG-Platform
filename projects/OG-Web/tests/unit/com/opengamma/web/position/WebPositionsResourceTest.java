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
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.master.security.ManageableSecurityLink;

/**
 * Test {@link WebPositionsResource}.
 */
public class WebPositionsResourceTest extends AbstractWebPositionResourceTestCase {
 
  @Test
  public void testAddPositionWithTrades() throws Exception {
    String tradesJson = getTradesJson();
    Response response = _webPositionsResource.postJSON("10", SEC_ID.getScheme().getName(), SEC_ID.getValue(), tradesJson);
    assertNotNull(response);
    assertEquals(201, response.getStatus());
    assertEquals("/positions/MemPos~1", getActualURL(response));
    assertPositionAndTrades();
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
    
    String allPositions = _webPositionsResource.getJSON(null, null, null, null, null, null, queryParameters.get("positionId"), queryParameters.get("tradeId"));
    assertNotNull(allPositions);
    assertJSONObjectEquals(loadJson("com/opengamma/web/position/allPositionsJson.txt"), new JSONObject(allPositions));
  }

  private String getActualURL(Response response) {
    return response.getMetadata().getFirst("Location").toString();
  }

  private void assertPositionAndTrades() {
    PositionSearchRequest request = new PositionSearchRequest();
    PositionSearchResult searchResult = _positionMaster.search(request);
    assertNotNull(searchResult);
    List<PositionDocument> docs = searchResult.getDocuments();
    assertNotNull(docs);
    assertEquals(1, docs.size());
    ManageablePosition position = docs.get(0).getPosition();
    assertEquals(BigDecimal.TEN, position.getQuantity());
    ManageableSecurityLink expectedSecurityLink = new ManageableSecurityLink(EQUITY_SECURITY.getExternalIdBundle());
    assertEquals(expectedSecurityLink, position.getSecurityLink());
    
    List<ManageableTrade> trades = position.getTrades();
    assertEquals(3, trades.size());
    for (ManageableTrade trade : trades) {
      assertEquals(expectedSecurityLink, trade.getSecurityLink());
      
      trade.setUniqueId(null);
      trade.setParentPositionId(null);
      trade.setSecurityLink(new ManageableSecurityLink(SEC_ID));
      assertTrue(_trades.contains(trade));
    }
   
  }
}
