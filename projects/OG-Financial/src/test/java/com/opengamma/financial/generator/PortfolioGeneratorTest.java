/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link PortfolioGenerator}, {@link LeafPortfolioNodeGenerator}, and {@link SimplePositionGenerator} classes.
 */
@Test(groups = TestGroup.UNIT)
public class PortfolioGeneratorTest {

  private PositionGenerator createSimplePositionGenerator(final InMemorySecuritySource source) {
    return new SimplePositionGenerator<RawSecurity>(new StaticQuantityGenerator(10), new SecurityGenerator<RawSecurity>() {
      @Override
      public RawSecurity createSecurity() {
        return new RawSecurity();
      }

      @Override
      public ManageableTrade createSecurityTrade(QuantityGenerator quantityGenerator, SecurityPersister securityPersister, NameGenerator counterPartyGenerator) {
        ManageableTrade trade = null;
        RawSecurity security = createSecurity();
        ZonedDateTime tradeDate = ZonedDateTime.now();
        trade = new ManageableTrade(quantityGenerator.createQuantity(), securityPersister.storeSecurity(security), tradeDate.toLocalDate(), tradeDate.toOffsetDateTime().toOffsetTime(),
            ExternalId.of(Counterparty.DEFAULT_SCHEME, counterPartyGenerator.createName()));
        return trade;
      }
      
    }, new InMemorySecurityPersister(source));
  }

  private void testPositions(final Collection<Position> positions, final SecuritySource source) {
    for (Position position : positions) {
      assertEquals(position.getQuantity().intValue(), 10);
      assertNull(position.getSecurity());
      assertNotNull(position.getSecurityLink().getExternalId());
      final Security security = position.getSecurityLink().resolve(source);
      assertNotNull(security);
      assertTrue(security instanceof RawSecurity);
    }
  }

  public void testSinglePosition() {
    final InMemorySecuritySource source = new InMemorySecuritySource();
    final PortfolioGenerator generator = new PortfolioGenerator(new LeafPortfolioNodeGenerator(new StaticNameGenerator("NODE"), createSimplePositionGenerator(source), 1), new StaticNameGenerator(
        "PORTFOLIO"));
    final Portfolio portfolio = generator.createPortfolio();
    assertEquals(portfolio.getRootNode().getChildNodes().size(), 0);
    assertEquals(portfolio.getRootNode().getPositions().size(), 1);
    testPositions(portfolio.getRootNode().getPositions(), source);
    assertEquals(portfolio.getRootNode().getName(), "NODE");
    assertEquals(portfolio.getName(), "PORTFOLIO");
  }

  public void testMultiplePosition() {
    final InMemorySecuritySource source = new InMemorySecuritySource();
    final PortfolioGenerator generator = new PortfolioGenerator(new LeafPortfolioNodeGenerator(new StaticNameGenerator("NODE"), createSimplePositionGenerator(source), 20), new StaticNameGenerator(
        "PORTFOLIO"));
    final Portfolio portfolio = generator.createPortfolio();
    assertEquals(portfolio.getRootNode().getChildNodes().size(), 0);
    assertEquals(portfolio.getRootNode().getPositions().size(), 20);
    testPositions(portfolio.getRootNode().getPositions(), source);
    assertEquals(portfolio.getRootNode().getName(), "NODE");
    assertEquals(portfolio.getName(), "PORTFOLIO");
  }

}
