/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.math.BigDecimal;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.OffsetTime;

import org.json.JSONObject;
import org.testng.annotations.Test;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.util.money.Currency;

import static com.opengamma.web.WebResourceTestUtils.*;
import static org.testng.AssertJUnit.assertNotNull;

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
}
