/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import java.math.BigDecimal;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import org.testng.annotations.Test;

import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Test {@link TradeFudgeBuilder}.
 */
@Test
public class TradeFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void testEmpty() {
    SimpleTrade trade = new SimpleTrade();
    assertEncodeDecodeCycle(Trade.class, trade);
  }
  
  public void testTrade() {
    SimpleTrade trade = new SimpleTrade();
    trade.setUniqueId(UniqueId.of("A", "B"));
    trade.setParentPositionId(UniqueId.of("C", "D"));
    trade.setQuantity(BigDecimal.valueOf(12.34d));
    trade.setSecurityLink(new SimpleSecurityLink(ExternalId.of("E", "F")));
    trade.setCounterparty(new SimpleCounterparty(ExternalId.of("G", "H")));
    trade.setTradeDate(LocalDate.of(2011, 1, 5));
    trade.setTradeTime(OffsetTime.parse("14:30+02:00"));
    assertEncodeDecodeCycle(Trade.class, trade);
  }

  public void testFull() {
    SimpleTrade trade = new SimpleTrade();
    trade.setUniqueId(UniqueId.of("A", "B"));
    trade.setParentPositionId(UniqueId.of("C", "D"));
    trade.setQuantity(BigDecimal.valueOf(12.34d));
    trade.setSecurityLink(new SimpleSecurityLink(ExternalId.of("E", "F")));
    trade.setCounterparty(new SimpleCounterparty(ExternalId.of("G", "H")));
    trade.setTradeDate(LocalDate.of(2011, 1, 5));
    trade.setTradeTime(OffsetTime.parse("14:30+02:00"));
    
    //set premium
    trade.setPremium(100.00);
    trade.setPremiumCurrency(Currency.USD);
    trade.setPremiumDate(LocalDate.of(2011, 1, 6));
    trade.setPremiumTime(OffsetTime.parse("15:30+02:00"));
    
    //set attributes
    trade.addAttribute("A", "B");
    trade.addAttribute("C", "D");
    assertEncodeDecodeCycle(Trade.class, trade);
  }
  
  public void testTrade_withPremium() {
    SimpleTrade trade = new SimpleTrade();
    trade.setUniqueId(UniqueId.of("A", "B"));
    trade.setParentPositionId(UniqueId.of("C", "D"));
    trade.setQuantity(BigDecimal.valueOf(12.34d));
    trade.setSecurityLink(new SimpleSecurityLink(ObjectId.of("E", "F")));
    trade.setCounterparty(new SimpleCounterparty(ExternalId.of("G", "H")));
    trade.setTradeDate(LocalDate.of(2011, 1, 5));
    trade.setTradeTime(OffsetTime.parse("14:30+02:00"));
    
    //set premium
    trade.setPremium(100.00);
    trade.setPremiumCurrency(Currency.USD);
    trade.setPremiumDate(LocalDate.of(2011, 1, 6));
    trade.setPremiumTime(OffsetTime.parse("15:30+02:00"));
    assertEncodeDecodeCycle(Trade.class, trade);
  }
  
  public void testTrade_withAttributes() {
    SimpleTrade trade = new SimpleTrade();
    trade.setUniqueId(UniqueId.of("A", "B"));
    trade.setParentPositionId(UniqueId.of("C", "D"));
    trade.setQuantity(BigDecimal.valueOf(12.34d));
    trade.setSecurityLink(new SimpleSecurityLink(ExternalId.of("E", "F")));
    trade.setCounterparty(new SimpleCounterparty(ExternalId.of("G", "H")));
    trade.setTradeDate(LocalDate.of(2011, 1, 5));
    trade.setTradeTime(OffsetTime.parse("14:30+02:00"));
    
    //set attributes
    trade.addAttribute("A", "B");
    trade.addAttribute("C", "D");
    assertEncodeDecodeCycle(Trade.class, trade);
  }

}
