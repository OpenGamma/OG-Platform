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

import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;

import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link WebPositionResource}.
 */
@Test(groups = TestGroup.UNIT)
public class WebPositionResourceTest extends AbstractWebPositionResourceTestCase {

  @Test
  public void testGetPositionWithTrades() throws Exception {
    final ManageableTrade trade = new ManageableTrade(BigDecimal.valueOf(50), SEC_ID, LocalDate.parse("2011-12-07"), OffsetTime.of(LocalTime.of(15, 4), ZONE_OFFSET), COUNTER_PARTY);
    trade.setPremium(10.0);
    trade.setPremiumCurrency(Currency.USD);
    trade.setPremiumDate(LocalDate.parse("2011-12-08"));
    trade.setPremiumTime(OffsetTime.of(LocalTime.of(15, 4), ZONE_OFFSET));

    final ManageablePosition manageablePosition = new ManageablePosition(trade.getQuantity(), SEC_ID);
    manageablePosition.addTrade(trade);
    final PositionDocument addedPos = _positionMaster.add(new PositionDocument(manageablePosition));

    final WebPositionResource positionResource = _webPositionsResource.findPosition(addedPos.getUniqueId().toString());
    final String json = positionResource.getJSON();
    assertNotNull(json);
    assertJSONObjectEquals(loadJson("com/opengamma/web/position/position.txt"), new JSONObject(json));
  }

  @Test
  public void testGetPositionWithoutTrade() throws Exception {
    final ManageablePosition manageablePosition = new ManageablePosition(BigDecimal.valueOf(50), SEC_ID);
    final PositionDocument addedPos = _positionMaster.add(new PositionDocument(manageablePosition));

    final WebPositionResource positionResource = _webPositionsResource.findPosition(addedPos.getUniqueId().toString());
    final String json = positionResource.getJSON();
    assertNotNull(json);
    assertJSONObjectEquals(loadJson("com/opengamma/web/position/positionWithoutTrades.txt"), new JSONObject(json));
  }

  @Test
  public void testUpdatePositionWithTrades() throws Exception {
    final UniqueId uid = addPosition();
    final WebPositionResource positionResource = _webPositionsResource.findPosition(uid.toString());

    final Response response = positionResource.putJSON(QUANTITY.toString(), getTradesJson(), null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    final PositionDocument positionDocument = _positionMaster.get(uid);
    assertNotNull(positionDocument);

    final ManageablePosition position = positionDocument.getPosition();
    assertEquals(BigDecimal.valueOf(QUANTITY), position.getQuantity());
    final List<ManageableTrade> trades = position.getTrades();
    assertEquals(3, trades.size());
    for (final ManageableTrade trade : trades) {
      assertEquals(SECURITY_LINK, trade.getSecurityLink());
      trade.setUniqueId(null);
      trade.setSecurityLink(new ManageableSecurityLink(SEC_ID));
      trade.setParentPositionId(null);
      assertTrue(_trades.contains(trade));
    }
  }

  @Test
  public void testUpdatePositionWithoutTrades() throws Exception {
    final UniqueId uid = addPosition();
    final WebPositionResource positionResource = _webPositionsResource.findPosition(uid.toString());

    final Response response = positionResource.putJSON(QUANTITY.toString(), null, null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    final PositionDocument positionDocument = _positionMaster.get(uid);
    assertNotNull(positionDocument);

    final ManageablePosition position = positionDocument.getPosition();
    assertEquals(BigDecimal.valueOf(QUANTITY), position.getQuantity());
    final List<ManageableTrade> trades = position.getTrades();
    assertTrue(trades.isEmpty());
  }

  @Test
  public void testUpdatePositionWithEmptyTrades() throws Exception {
    final UniqueId uid = addPosition();
    final WebPositionResource positionResource = _webPositionsResource.findPosition(uid.toString());

    final Response response = positionResource.putJSON(QUANTITY.toString(), EMPTY_TRADES, null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    final PositionDocument positionDocument = _positionMaster.get(uid);
    assertNotNull(positionDocument);

    final ManageablePosition position = positionDocument.getPosition();
    assertEquals(BigDecimal.valueOf(QUANTITY), position.getQuantity());
    final List<ManageableTrade> trades = position.getTrades();
    assertTrue(trades.isEmpty());
  }
}
