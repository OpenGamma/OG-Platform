/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.testng.annotations.Test;
import java.math.BigDecimal;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.CounterpartyImpl;
import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.money.Currency;

/**
 * Test the {@link TradeBuilder} class.
 */
@Test
public class TradeBuilderTest extends AbstractBuilderTestCase {

  public void testEmpty() {
    TradeImpl trade = new TradeImpl();
    assertEncodeDecodeCycle(Trade.class, trade);
  }

  public void testFull() {
    TradeImpl trade = new TradeImpl();
    trade.setUniqueId(UniqueIdentifier.of("A", "B"));
    trade.setParentPositionId(UniqueIdentifier.of("C", "D"));
    trade.setQuantity(BigDecimal.valueOf(12.34d));
    trade.setSecurityKey(IdentifierBundle.of(Identifier.of("E", "F")));
    trade.setCounterparty(new CounterpartyImpl(Identifier.of("G", "H")));
    trade.setTradeDate(LocalDate.of(2011, 1, 5));
    trade.setTradeTime(OffsetTime.parse("14:30+02:00"));
    
    //set premium
    trade.setPremium(100.00);
    trade.setPremiumCurrency(Currency.USD);
    trade.setPremiumDate(LocalDate.of(2011, 1, 6));
    trade.setPremiumTime(OffsetTime.parse("15:30+02:00"));
    assertEncodeDecodeCycle(Trade.class, trade);
  }

}
