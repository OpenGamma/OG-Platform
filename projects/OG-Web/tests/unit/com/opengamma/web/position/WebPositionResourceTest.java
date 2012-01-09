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
import java.util.List;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.OffsetTime;
import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.testng.annotations.Test;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.money.Currency;

/**
 * Test {@link WebPositionResource}.
 */
public class WebPositionResourceTest extends AbstractWebPositionResourceTestCase {

  @Test
  public void testGetPositionWithTrades() throws Exception {
    ManageableTrade trade = new ManageableTrade(BigDecimal.valueOf(50), SEC_ID, LocalDate.parse("2011-12-07"), OffsetTime.of(LocalTime.of(15, 4), ZONE_OFFSET), COUNTER_PARTY);
    trade.setPremium(10.0);
    trade.setPremiumCurrency(Currency.USD);
    trade.setPremiumDate(LocalDate.parse("2011-12-08"));
    trade.setPremiumTime(OffsetTime.of(LocalTime.of(15, 4), ZONE_OFFSET));
    
    ManageablePosition manageablePosition = new ManageablePosition(trade.getQuantity(), SEC_ID);
    manageablePosition.addTrade(trade);
    PositionDocument addedPos = _positionMaster.add(new PositionDocument(manageablePosition));
    
    WebPositionResource positionResource = _webPositionsResource.findPosition(addedPos.getUniqueId().toString());
    
    String json = positionResource.getJSON();
    assertNotNull(json);
    assertJSONObjectEquals(loadJson("com/opengamma/web/position/position.txt"), new JSONObject(json));
  }
  
  @Test
  public void testGetPositionWithoutTrade() throws Exception {
    ManageablePosition manageablePosition = new ManageablePosition(BigDecimal.valueOf(50), SEC_ID);
    PositionDocument addedPos = _positionMaster.add(new PositionDocument(manageablePosition));
    
    WebPositionResource positionResource = _webPositionsResource.findPosition(addedPos.getUniqueId().toString());
    
    String json = positionResource.getJSON();
    assertNotNull(json);
    assertJSONObjectEquals(loadJson("com/opengamma/web/position/positionWithoutTrades.txt"), new JSONObject(json));
  }
  
  @Test
  public void testUpdatePositionTrades() throws Exception {
    ManageableTrade origTrade = new ManageableTrade(BigDecimal.valueOf(50), SEC_ID, LocalDate.parse("2011-12-07"), OffsetTime.of(LocalTime.of(15, 4), ZONE_OFFSET), COUNTER_PARTY);
    origTrade.setPremium(10.0);
    origTrade.setPremiumCurrency(Currency.USD);
    origTrade.setPremiumDate(LocalDate.parse("2011-12-08"));
    origTrade.setPremiumTime(OffsetTime.of(LocalTime.of(15, 4), ZONE_OFFSET));
    
    ManageablePosition manageablePosition = new ManageablePosition(origTrade.getQuantity(), SEC_ID);
    manageablePosition.addTrade(origTrade);
    PositionDocument addedPos = _positionMaster.add(new PositionDocument(manageablePosition));
    UniqueId uid = addedPos.getUniqueId();
    
    WebPositionResource positionResource = _webPositionsResource.findPosition(uid.toString());
    
    Long updatedQuantity = Long.valueOf(100);
    Response response = positionResource.putJSON(updatedQuantity.toString(), getTradesJson());
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    PositionDocument positionDocument = _positionMaster.get(uid);
    assertNotNull(positionDocument);
    
    ManageablePosition position = positionDocument.getPosition();
    assertEquals(BigDecimal.valueOf(updatedQuantity), position.getQuantity());
    List<ManageableTrade> trades = position.getTrades();
    assertEquals(3, trades.size());
    ManageableSecurityLink expectedSecurityLink = new ManageableSecurityLink(EQUITY_SECURITY.getExternalIdBundle().getExternalId(SecurityUtils.BLOOMBERG_TICKER));
    for (ManageableTrade trade : trades) {
      assertEquals(expectedSecurityLink, trade.getSecurityLink());
      
      trade.setUniqueId(null);
      trade.setParentPositionId(null);
      trade.setSecurityLink(new ManageableSecurityLink(SEC_ID));
      assertTrue(_trades.contains(trade));
    }
    
  }
}
