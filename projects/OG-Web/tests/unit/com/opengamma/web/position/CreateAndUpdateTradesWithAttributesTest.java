package com.opengamma.web.position;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;
import javax.ws.rs.core.Response;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Maps;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.money.Currency;
import com.opengamma.web.WebResourceTestUtils;

public class CreateAndUpdateTradesWithAttributesTest extends AbstractWebPositionResourceTestCase {
  
  private static final OffsetTime TRADE_TIME = OffsetTime.parse("19:04+02:00");
  private static final LocalDate TRADE_DATE = LocalDate.parse("2011-12-10");
  private static final BigDecimal TRADE_QUANTITY = BigDecimal.valueOf(80);
  private static final OffsetTime PREMIUM_TIME = OffsetTime.parse("18:04+01:00");
  private static final LocalDate PREMIUM_DATE = LocalDate.parse("2011-12-11");
  private static final String COUNTER_PARTY = "BACS";
  private static final double TRADE_PREMIUM = 40.0;
  
  private Map<String, String> _dealAttributes;
  private Map<String, String> _userAttributes;
  private UniqueId _positionID;
  
  @BeforeMethod
  public void init() {
    _positionID = addPosition();
    
    _dealAttributes = Maps.newHashMap();
    _dealAttributes.put("deal1", "value1");
    _dealAttributes.put("deal2", "value2");
    
    _userAttributes = Maps.newHashMap();
    _userAttributes.put("user1", "value1");
    _userAttributes.put("user2", "value2");
  }

  @Test
  public void updateTradeWithAttributes() throws Exception {
    WebPositionResource positionResource = _webPositionsResource.findPosition(_positionID.toString());
    
    Response response = positionResource.putJSON(QUANTITY.toString(), WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithAttributes.txt").toString());
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    PositionDocument positionDocument = _positionMaster.get(_positionID);
    ManageableTrade trade = assertTrade(positionDocument);
    Map<String, String> attributes = trade.getAttributes();
    assertNotNull(attributes);
    assertEquals(4, attributes.size());
    assertDealAttributes(attributes);
    assertUserAttributes(attributes);
  }
  
  @Test
  public void updateTradeWithEmptyDealAttributes() throws Exception {
    WebPositionResource positionResource = _webPositionsResource.findPosition(_positionID.toString());
    
    Response response = positionResource.putJSON(QUANTITY.toString(), WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithEmptyDealAttributes.txt").toString());
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    PositionDocument positionDocument = _positionMaster.get(_positionID);
    ManageableTrade trade = assertTrade(positionDocument);
    Map<String, String> attributes = trade.getAttributes();
    assertNotNull(attributes);
    assertEquals(2, attributes.size());
    assertUserAttributes(attributes);
  }
  
  @Test
  public void updateTradeWithEmptyUserAttributes() throws Exception {
    WebPositionResource positionResource = _webPositionsResource.findPosition(_positionID.toString());
    
    Response response = positionResource.putJSON(QUANTITY.toString(), WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithEmptyUserAttributes.txt").toString());
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    PositionDocument positionDocument = _positionMaster.get(_positionID);
    ManageableTrade trade = assertTrade(positionDocument);
    Map<String, String> attributes = trade.getAttributes();
    assertNotNull(attributes);
    assertEquals(2, attributes.size());
    assertDealAttributes(attributes);
  }
  
  @Test
  public void updateTradeWithNoDealAttributes() throws Exception {
    WebPositionResource positionResource = _webPositionsResource.findPosition(_positionID.toString());
    
    Response response = positionResource.putJSON(QUANTITY.toString(), WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithNoDealAttributes.txt").toString());
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    PositionDocument positionDocument = _positionMaster.get(_positionID);
    ManageableTrade trade = assertTrade(positionDocument);
    Map<String, String> attributes = trade.getAttributes();
    assertNotNull(attributes);
    assertEquals(2, attributes.size());
    assertUserAttributes(attributes);
  }
  
  @Test
  public void updateTradeWithNoUserAttributes() throws Exception {
    WebPositionResource positionResource = _webPositionsResource.findPosition(_positionID.toString());
    
    Response response = positionResource.putJSON(QUANTITY.toString(), WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithNoUserAttributes.txt").toString());
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    PositionDocument positionDocument = _positionMaster.get(_positionID);
    ManageableTrade trade = assertTrade(positionDocument);
    Map<String, String> attributes = trade.getAttributes();
    assertNotNull(attributes);
    assertEquals(2, attributes.size());
    assertDealAttributes(attributes);
  }

  private void assertUserAttributes(Map<String, String> attributes) {
    for (Entry<String, String> attrEntry : _userAttributes.entrySet()) {
      assertEquals(attrEntry.getValue(), attributes.get(attrEntry.getKey()));
    }
  }

  private void assertDealAttributes(Map<String, String> attributes) {
    for (Entry<String, String> attrEntry : _dealAttributes.entrySet()) {
      assertEquals(attrEntry.getValue(), attributes.get(attrEntry.getKey()));
    }
  }

  @Test
  public void updateTradeWithEmptyAttributes() throws Exception {
    WebPositionResource positionResource = _webPositionsResource.findPosition(_positionID.toString());
    Response response = positionResource.putJSON(QUANTITY.toString(), WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithEmptyAttributes.txt").toString());
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    PositionDocument positionDocument = _positionMaster.get(_positionID);
    ManageableTrade trade = assertTrade(positionDocument);
    assertTrue(trade.getAttributes().isEmpty());
  }

  @Test
  public void updateTradeWithNoAttributes() throws Exception {
    WebPositionResource positionResource = _webPositionsResource.findPosition(_positionID.toString());
    Response response = positionResource.putJSON(QUANTITY.toString(), WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithNoAttributes.txt").toString());
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    PositionDocument positionDocument = _positionMaster.get(_positionID);
    ManageableTrade trade = assertTrade(positionDocument);
    assertTrue(trade.getAttributes().isEmpty());
  }
  
  private ManageableTrade assertTrade(PositionDocument positionDocument) {
    assertNotNull(positionDocument);
    
    ManageablePosition position = positionDocument.getPosition();
    assertEquals(BigDecimal.valueOf(QUANTITY), position.getQuantity());
    assertEquals(1, position.getTrades().size());
    ManageableTrade trade = position.getTrades().iterator().next();
    ManageableSecurityLink expectedSecurityLink = new ManageableSecurityLink(EQUITY_SECURITY.getExternalIdBundle().getExternalId(SecurityUtils.BLOOMBERG_TICKER));
    
    assertEquals(expectedSecurityLink, trade.getSecurityLink());
    assertEquals(TRADE_PREMIUM, trade.getPremium());
    assertEquals(COUNTER_PARTY, trade.getCounterpartyExternalId().getValue());
    assertEquals(Currency.USD, trade.getPremiumCurrency());
    assertEquals(PREMIUM_DATE, trade.getPremiumDate());
    assertEquals(PREMIUM_TIME, trade.getPremiumTime());
    assertEquals(TRADE_QUANTITY, trade.getQuantity());
    assertEquals(TRADE_DATE, trade.getTradeDate());
    assertEquals(TRADE_TIME, trade.getTradeTime());
    return trade;
  }
}
