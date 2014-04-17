package com.opengamma.web.position;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.google.common.collect.Maps;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.WebResourceTestUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CreateAndUpdateTradesWithAttributesTest extends AbstractWebPositionResourceTestCase {
  
  private static final OffsetTime TRADE_TIME = OffsetTime.parse("19:04+02:00");
  private static final LocalDate TRADE_DATE = LocalDate.parse("2011-12-10");
  private static final BigDecimal TRADE_QUANTITY = BigDecimal.valueOf(80);
  private static final OffsetTime PREMIUM_TIME = OffsetTime.parse("18:04+01:00");
  private static final LocalDate PREMIUM_DATE = LocalDate.parse("2011-12-11");
  private static final String COUNTER_PARTY = "BACS";
  private static final double TRADE_PREMIUM = 40.0;
  
  private static final Pattern s_urlPattern = Pattern.compile("^(/positions/)(MemPos~[0-9]+)$");
  
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
    
    Response response = positionResource.putJSON(QUANTITY.toString(), 
        WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithAttributes.txt").toString(), null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    assertTradeWithAttributes(_positionID);
  }
  
  @Test
  public void createTradeWithAttributes() throws Exception {
    Response response = _webPositionsResource.postJSON(QUANTITY.toString(), SEC_ID.getScheme().getName(), SEC_ID.getValue(), 
        WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithAttributes.txt").toString(), null, null, null);
    assertNotNull(response);
    assertEquals(201, response.getStatus());
    String actualURL = getActualURL(response);
    Matcher matcher = s_urlPattern.matcher(actualURL);
    assertTrue(matcher.matches());
    String positionId = matcher.group(2);
    assertTradeWithAttributes(UniqueId.parse(positionId));
  }

  private void assertTradeWithAttributes(UniqueId positionId) {
    PositionDocument positionDocument = _positionMaster.get(positionId);
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
    
    Response response = positionResource.putJSON(QUANTITY.toString(), 
        WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithEmptyDealAttributes.txt").toString(), null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    assertTradeWithEmptyDealAttributes(_positionID);
  }

  @Test
  public void createTradeWithEmptyDealAttributes() throws Exception {
    Response response = _webPositionsResource.postJSON(QUANTITY.toString(), SEC_ID.getScheme().getName(), SEC_ID.getValue(), 
        WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithEmptyDealAttributes.txt").toString(), null, null, null);
    assertNotNull(response);
    assertEquals(201, response.getStatus());
    String actualURL = getActualURL(response);
    Matcher matcher = s_urlPattern.matcher(actualURL);
    assertTrue(matcher.matches());
    String positionId = matcher.group(2);
    assertTradeWithEmptyDealAttributes(UniqueId.parse(positionId));
  }
  
  @Test
  public void updateTradeWithNoDealAttributes() throws Exception {
    WebPositionResource positionResource = _webPositionsResource.findPosition(_positionID.toString());
    
    Response response = positionResource.putJSON(QUANTITY.toString(), 
        WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithNoDealAttributes.txt").toString(), null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    assertTradeWithEmptyDealAttributes(_positionID);
  }
  
  @Test
  public void createTradeWithNoDealAttributes() throws Exception {
    Response response = _webPositionsResource.postJSON(QUANTITY.toString(), SEC_ID.getScheme().getName(), SEC_ID.getValue(), 
        WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithNoDealAttributes.txt").toString(), null, null, null);
    assertNotNull(response);
    assertEquals(201, response.getStatus());
    String actualURL = getActualURL(response);
    Matcher matcher = s_urlPattern.matcher(actualURL);
    assertTrue(matcher.matches());
    String positionId = matcher.group(2);
    assertTradeWithEmptyDealAttributes(UniqueId.parse(positionId));
  }
  
  private void assertTradeWithEmptyDealAttributes(UniqueId positionId) {
    PositionDocument positionDocument = _positionMaster.get(positionId);
    ManageableTrade trade = assertTrade(positionDocument);
    Map<String, String> attributes = trade.getAttributes();
    assertNotNull(attributes);
    assertEquals(2, attributes.size());
    assertUserAttributes(attributes);
  }
  
  @Test
  public void updateTradeWithEmptyUserAttributes() throws Exception {
    WebPositionResource positionResource = _webPositionsResource.findPosition(_positionID.toString());
    
    Response response = positionResource.putJSON(QUANTITY.toString(), 
        WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithEmptyUserAttributes.txt").toString(), null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    assertTradeWithEmptyUserAttributes(_positionID);
    
  }
  
  @Test
  public void createTradeWithEmptyUserAttributes() throws Exception {
    Response response = _webPositionsResource.postJSON(QUANTITY.toString(), SEC_ID.getScheme().getName(), SEC_ID.getValue(), 
        WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithEmptyUserAttributes.txt").toString(), null, null, null);
    assertNotNull(response);
    assertEquals(201, response.getStatus());
    String actualURL = getActualURL(response);
    Matcher matcher = s_urlPattern.matcher(actualURL);
    assertTrue(matcher.matches());
    String positionId = matcher.group(2);
    assertTradeWithEmptyUserAttributes(UniqueId.parse(positionId));
  }
  
  @Test
  public void updateTradeWithNoUserAttributes() throws Exception {
    WebPositionResource positionResource = _webPositionsResource.findPosition(_positionID.toString());
    
    Response response = positionResource.putJSON(QUANTITY.toString(), 
        WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithNoUserAttributes.txt").toString(), null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    assertTradeWithEmptyUserAttributes(_positionID);
  }
  
  @Test
  public void createTradeWithNoUserAttributes() throws Exception {
    Response response = _webPositionsResource.postJSON(QUANTITY.toString(), SEC_ID.getScheme().getName(), SEC_ID.getValue(), 
        WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithNoUserAttributes.txt").toString(), null, null, null);
    assertNotNull(response);
    assertEquals(201, response.getStatus());
    String actualURL = getActualURL(response);
    Matcher matcher = s_urlPattern.matcher(actualURL);
    assertTrue(matcher.matches());
    String positionId = matcher.group(2);
    assertTradeWithEmptyUserAttributes(UniqueId.parse(positionId));
  }
  
  private void assertTradeWithEmptyUserAttributes(UniqueId positionId) {
    PositionDocument positionDocument = _positionMaster.get(positionId);
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
    Response response = positionResource.putJSON(QUANTITY.toString(), 
        WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithEmptyAttributes.txt").toString(), null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    assertTradeWithEmptyAttributes(_positionID);
  }
  
  @Test
  public void createTradeWithEmptyAttributes() throws Exception {
    Response response = _webPositionsResource.postJSON(QUANTITY.toString(), SEC_ID.getScheme().getName(), SEC_ID.getValue(), 
        WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithEmptyAttributes.txt").toString(), null, null, null);
    assertNotNull(response);
    assertEquals(201, response.getStatus());
    String actualURL = getActualURL(response);
    Matcher matcher = s_urlPattern.matcher(actualURL);
    assertTrue(matcher.matches());
    String positionId = matcher.group(2);
    
    assertTradeWithEmptyAttributes(UniqueId.parse(positionId));
  }

  @Test
  public void updateTradeWithNoAttributes() throws Exception {
    WebPositionResource positionResource = _webPositionsResource.findPosition(_positionID.toString());
    Response response = positionResource.putJSON(QUANTITY.toString(), 
        WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithNoAttributes.txt").toString(), null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    assertTradeWithEmptyAttributes(_positionID);
  }
  
  @Test
  public void createTradeWithNoAttributes() throws Exception {
    Response response = _webPositionsResource.postJSON(QUANTITY.toString(), SEC_ID.getScheme().getName(), SEC_ID.getValue(), 
        WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesWithNoAttributes.txt").toString(), null, null, null);
    assertNotNull(response);
    assertEquals(201, response.getStatus());
    String actualURL = getActualURL(response);
    Matcher matcher = s_urlPattern.matcher(actualURL);
    assertTrue(matcher.matches());
    String positionId = matcher.group(2);
    
    assertTradeWithEmptyAttributes(UniqueId.parse(positionId));
  }
  
  private void assertTradeWithEmptyAttributes(UniqueId positionId) {
    PositionDocument positionDocument = _positionMaster.get(positionId);
    ManageableTrade trade = assertTrade(positionDocument);
    assertTrue(trade.getAttributes().isEmpty());
  }
  
  private ManageableTrade assertTrade(PositionDocument positionDocument) {
    assertNotNull(positionDocument);
    
    ManageablePosition position = positionDocument.getPosition();
    assertEquals(BigDecimal.valueOf(QUANTITY), position.getQuantity());
    assertEquals(1, position.getTrades().size());
    ManageableTrade trade = position.getTrades().iterator().next();
    
    assertEquals(SECURITY_LINK, trade.getSecurityLink());
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
