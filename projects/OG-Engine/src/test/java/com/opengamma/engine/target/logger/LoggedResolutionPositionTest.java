/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.logger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.core.position.Position;
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
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link LoggedResolutionPosition} class.
 */
@Test(groups = TestGroup.UNIT)
public class LoggedResolutionPositionTest {

  public void getQuantity() {
    final Position position = Mockito.mock(Position.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Position logged = new LoggedResolutionPosition(position, logger);
    Mockito.when(position.getQuantity()).thenReturn(BigDecimal.ONE);
    assertEquals(logged.getQuantity(), BigDecimal.ONE);
    Mockito.verifyZeroInteractions(logger);
  }

  public void getSecurityLink() {
    final Position position = Mockito.mock(Position.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Position logged = new LoggedResolutionPosition(position, logger);
    final SecurityLink link = Mockito.mock(SecurityLink.class);
    Mockito.when(position.getSecurityLink()).thenReturn(link);
    assertSame(logged.getSecurityLink(), link);
    Mockito.verifyZeroInteractions(logger);
  }

  public void getSecurity_externalId() {
    final Position position = Mockito.mock(Position.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Position logged = new LoggedResolutionPosition(position, logger);
    final Security security = Mockito.mock(Security.class);
    Mockito.when(security.getUniqueId()).thenReturn(UniqueId.of("Foo", "Bar", "Cow"));
    Mockito.when(position.getSecurity()).thenReturn(security);
    Mockito.when(position.getSecurityLink()).thenReturn(new SimpleSecurityLink(ExternalId.of("Foo", "Bar")));
    assertSame(logged.getSecurity(), security);
    Mockito.verify(logger).log(new ComputationTargetRequirement(ComputationTargetType.SECURITY, ExternalId.of("Foo", "Bar")), UniqueId.of("Foo", "Bar", "Cow"));
    Mockito.verifyNoMoreInteractions(logger);
  }

  public void getSecurity_objectId() {
    final Position position = Mockito.mock(Position.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Position logged = new LoggedResolutionPosition(position, logger);
    final Security security = Mockito.mock(Security.class);
    Mockito.when(security.getUniqueId()).thenReturn(UniqueId.of("Foo", "Bar", "Cow"));
    Mockito.when(position.getSecurity()).thenReturn(security);
    Mockito.when(position.getSecurityLink()).thenReturn(new SimpleSecurityLink(ObjectId.of("Foo", "Bar")));
    assertSame(logged.getSecurity(), security);
    Mockito.verify(logger).log(new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Foo", "Bar")), UniqueId.of("Foo", "Bar", "Cow"));
    Mockito.verifyNoMoreInteractions(logger);
  }

  public void getTrades() {
    final Position position = Mockito.mock(Position.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Position logged = new LoggedResolutionPosition(position, logger);
    final List<Trade> trades = new ArrayList<Trade>();
    for (int i = 0; i < 3; i++) {
      final Trade trade = Mockito.mock(Trade.class);
      Mockito.when(trade.getUniqueId()).thenReturn(UniqueId.of("Trade", Integer.toString(i), "0"));
      trades.add(trade);
    }
    Mockito.when(position.getTrades()).thenReturn(trades);
    final Collection<Trade> loggedTrades = logged.getTrades();
    assertEquals(loggedTrades.size(), 3);
    int i = 0;
    for (Trade trade : loggedTrades) {
      assertTrue(trade instanceof LoggedResolutionTrade);
      // Mockito.verify(logger).log(new ComputationTargetSpecification(ComputationTargetType.TRADE, UniqueId.of("Trade", Integer.toString(i))), UniqueId.of("Trade", Integer.toString(i), "0"));
      i++;
    }
    Mockito.verifyNoMoreInteractions(logger);
  }

  @SuppressWarnings("unchecked")
  public void getAttributes() {
    final Position position = Mockito.mock(Position.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Position logged = new LoggedResolutionPosition(position, logger);
    final Map<String, String> attributes = Mockito.mock(Map.class);
    Mockito.when(position.getAttributes()).thenReturn(attributes);
    assertSame(logged.getAttributes(), attributes);
    Mockito.verifyZeroInteractions(logger);
  }

}
