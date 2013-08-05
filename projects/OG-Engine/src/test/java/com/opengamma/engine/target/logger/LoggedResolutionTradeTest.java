/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.logger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.math.BigDecimal;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link LoggedResolutionTrade} class.
 */
@Test(groups = TestGroup.UNIT)
public class LoggedResolutionTradeTest {

  public void getQuantity() {
    final Trade trade = Mockito.mock(Trade.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Trade logged = new LoggedResolutionTrade(trade, logger);
    Mockito.when(trade.getQuantity()).thenReturn(BigDecimal.ONE);
    assertEquals(logged.getQuantity(), BigDecimal.ONE);
    Mockito.verifyZeroInteractions(logger);
  }

  public void getSecurityLink() {
    final Trade trade = Mockito.mock(Trade.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Trade logged = new LoggedResolutionTrade(trade, logger);
    final SecurityLink link = Mockito.mock(SecurityLink.class);
    Mockito.when(trade.getSecurityLink()).thenReturn(link);
    assertSame(logged.getSecurityLink(), link);
    Mockito.verifyZeroInteractions(logger);
  }

  public void getSecurity_externalId() {
    final Trade trade = Mockito.mock(Trade.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Trade logged = new LoggedResolutionTrade(trade, logger);
    final Security security = Mockito.mock(Security.class);
    Mockito.when(security.getUniqueId()).thenReturn(UniqueId.of("Foo", "Bar", "Cow"));
    Mockito.when(trade.getSecurity()).thenReturn(security);
    Mockito.when(trade.getSecurityLink()).thenReturn(new SimpleSecurityLink(ExternalId.of("Foo", "Bar")));
    assertSame(logged.getSecurity(), security);
    Mockito.verify(logger).log(new ComputationTargetRequirement(ComputationTargetType.SECURITY, ExternalId.of("Foo", "Bar")), UniqueId.of("Foo", "Bar", "Cow"));
    Mockito.verifyNoMoreInteractions(logger);
  }

  public void getSecurity_objectId() {
    final Trade trade = Mockito.mock(Trade.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Trade logged = new LoggedResolutionTrade(trade, logger);
    final Security security = Mockito.mock(Security.class);
    Mockito.when(security.getUniqueId()).thenReturn(UniqueId.of("Foo", "Bar", "Cow"));
    Mockito.when(trade.getSecurity()).thenReturn(security);
    Mockito.when(trade.getSecurityLink()).thenReturn(new SimpleSecurityLink(ObjectId.of("Foo", "Bar")));
    assertSame(logged.getSecurity(), security);
    Mockito.verify(logger).log(new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Foo", "Bar")), UniqueId.of("Foo", "Bar", "Cow"));
    Mockito.verifyNoMoreInteractions(logger);
  }

  @SuppressWarnings("unchecked")
  public void getAttributes() {
    final Trade trade = Mockito.mock(Trade.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Trade logged = new LoggedResolutionTrade(trade, logger);
    final Map<String, String> attributes = Mockito.mock(Map.class);
    Mockito.when(trade.getAttributes()).thenReturn(attributes);
    assertSame(logged.getAttributes(), attributes);
    Mockito.verifyZeroInteractions(logger);
  }

  @SuppressWarnings("unchecked")
  public void setAttributes() {
    final Trade trade = Mockito.mock(Trade.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Trade logged = new LoggedResolutionTrade(trade, logger);
    final Map<String, String> param = Mockito.mock(Map.class);
    logged.setAttributes(param);
    Mockito.verify(trade).setAttributes(param);
    Mockito.verifyZeroInteractions(logger);
  }

  public void addAttribute() {
    final Trade trade = Mockito.mock(Trade.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Trade logged = new LoggedResolutionTrade(trade, logger);
    logged.addAttribute("Foo", "Bar");
    Mockito.verify(trade).addAttribute("Foo", "Bar");
    Mockito.verifyZeroInteractions(logger);
  }

  public void getCounterparty() {
    final Trade trade = Mockito.mock(Trade.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Trade logged = new LoggedResolutionTrade(trade, logger);
    final Counterparty result = Mockito.mock(Counterparty.class);
    Mockito.when(trade.getCounterparty()).thenReturn(result);
    assertSame(logged.getCounterparty(), result);
    Mockito.verifyZeroInteractions(logger);
  }

  public void getTradeDate() {
    final Trade trade = Mockito.mock(Trade.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Trade logged = new LoggedResolutionTrade(trade, logger);
    final LocalDate result = LocalDate.now();
    Mockito.when(trade.getTradeDate()).thenReturn(result);
    assertSame(logged.getTradeDate(), result);
    Mockito.verifyZeroInteractions(logger);
  }

  public void getTradeTime() {
    final Trade trade = Mockito.mock(Trade.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Trade logged = new LoggedResolutionTrade(trade, logger);
    final OffsetTime result = OffsetTime.now();
    Mockito.when(trade.getTradeTime()).thenReturn(result);
    assertSame(logged.getTradeTime(), result);
    Mockito.verifyZeroInteractions(logger);
  }

  public void getPremium() {
    final Trade trade = Mockito.mock(Trade.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Trade logged = new LoggedResolutionTrade(trade, logger);
    final Double result = 42d;
    Mockito.when(trade.getPremium()).thenReturn(result);
    assertSame(logged.getPremium(), result);
    Mockito.verifyZeroInteractions(logger);
  }

  public void getPremiumCurrency() {
    final Trade trade = Mockito.mock(Trade.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Trade logged = new LoggedResolutionTrade(trade, logger);
    final Currency result = Currency.USD;
    Mockito.when(trade.getPremiumCurrency()).thenReturn(result);
    assertSame(logged.getPremiumCurrency(), result);
    Mockito.verifyZeroInteractions(logger);
  }

  public void getPremiumDate() {
    final Trade trade = Mockito.mock(Trade.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Trade logged = new LoggedResolutionTrade(trade, logger);
    final LocalDate result = LocalDate.now();
    Mockito.when(trade.getPremiumDate()).thenReturn(result);
    assertSame(logged.getPremiumDate(), result);
    Mockito.verifyZeroInteractions(logger);
  }

  public void getPremiumTime() {
    final Trade trade = Mockito.mock(Trade.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Trade logged = new LoggedResolutionTrade(trade, logger);
    final OffsetTime result = OffsetTime.now();
    Mockito.when(trade.getPremiumTime()).thenReturn(result);
    assertSame(logged.getPremiumTime(), result);
    Mockito.verifyZeroInteractions(logger);
  }

}
