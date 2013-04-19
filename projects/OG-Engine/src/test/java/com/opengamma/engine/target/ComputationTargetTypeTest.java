/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;

import org.testng.annotations.Test;
import org.threeten.bp.OffsetDateTime;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test ComputationTargetType.
 */
@Test(groups = TestGroup.UNIT)
public class ComputationTargetTypeTest {

  private static final SimplePortfolioNode NODE = new SimplePortfolioNode();
  private static final Position POSITION = new SimplePosition(UniqueId.of("Test", "1"), new BigDecimal(1), ExternalIdBundle.EMPTY);
  private static final Security SECURITY = new SimpleSecurity("");
  private static final OffsetDateTime TRADE_OFFSET_DATETIME = OffsetDateTime.now();
  private static final Trade TRADE = new SimpleTrade(SECURITY, new BigDecimal(1),
      new SimpleCounterparty(ExternalId.of("CPARTY", "C100")), TRADE_OFFSET_DATETIME.toLocalDate(),
      TRADE_OFFSET_DATETIME.toOffsetTime());

  public void testIsCompatible_null() {
    assertTrue(ComputationTargetType.NULL.isCompatible((UniqueIdentifiable) null));
    assertFalse(ComputationTargetType.NULL.isCompatible(Currency.USD));
    assertTrue(ComputationTargetType.NULL.isCompatible(ComputationTargetType.NULL));
    assertFalse(ComputationTargetType.NULL.isCompatible(ComputationTargetType.PRIMITIVE));
    assertFalse(ComputationTargetType.NULL.isCompatible(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION)));
    assertFalse(ComputationTargetType.NULL.isCompatible(ComputationTargetType.POSITION.or(ComputationTargetType.TRADE)));
  }

  public void testIsCompatible_class() {
    assertFalse(ComputationTargetType.SECURITY.isCompatible((Security) null));
    assertFalse(ComputationTargetType.SECURITY.isCompatible(NODE));
    assertTrue(ComputationTargetType.SECURITY.isCompatible(SECURITY));
    assertFalse(ComputationTargetType.SECURITY.isCompatible(ComputationTargetType.NULL));
    assertTrue(ComputationTargetType.SECURITY.isCompatible(ComputationTargetType.POSITION.containing(ComputationTargetType.SECURITY)));
    assertFalse(ComputationTargetType.SECURITY.isCompatible(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION)));
    assertTrue(ComputationTargetType.SECURITY.isCompatible(ComputationTargetType.PRIMITIVE.or(ComputationTargetType.SECURITY)));
    assertFalse(ComputationTargetType.SECURITY.isCompatible(ComputationTargetType.POSITION.or(ComputationTargetType.TRADE)));
  }

  public void testIsCompatible_nested() {
    final ComputationTargetType t = ComputationTargetType.POSITION.containing(ComputationTargetType.TRADE);
    assertFalse(t.isCompatible((Position) null));
    assertTrue(t.isCompatible(TRADE));
    assertFalse(t.isCompatible(POSITION));
    assertFalse(t.isCompatible(ComputationTargetType.NULL));
    assertFalse(t.isCompatible(ComputationTargetType.POSITION));
    assertFalse(t.isCompatible(ComputationTargetType.TRADE));
    assertTrue(t.isCompatible(ComputationTargetType.POSITION.containing(ComputationTargetType.TRADE)));
    assertTrue(t.isCompatible(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION).containing(ComputationTargetType.TRADE)));
    assertFalse(t.isCompatible(ComputationTargetType.POSITION.containing(ComputationTargetType.TRADE).containing(ComputationTargetType.SECURITY)));
    assertFalse(t.containing(ComputationTargetType.SECURITY).isCompatible(ComputationTargetType.TRADE.containing(ComputationTargetType.SECURITY)));
    assertFalse(t.isCompatible(ComputationTargetType.POSITION.or(ComputationTargetType.TRADE)));
    assertFalse(t.isCompatible(ComputationTargetType.PRIMITIVE.or(ComputationTargetType.SECURITY)));
  }

  public void testIsCompatible_multiple() {
    final ComputationTargetType t = ComputationTargetType.POSITION.or(ComputationTargetType.TRADE);
    assertFalse(t.isCompatible((Trade) null));
    assertTrue(t.isCompatible(POSITION));
    assertTrue(t.isCompatible(TRADE));
    assertFalse(t.isCompatible(SECURITY));
    assertFalse(t.isCompatible(ComputationTargetType.NULL));
    assertFalse(t.isCompatible(ComputationTargetType.SECURITY));
    assertTrue(t.isCompatible(ComputationTargetType.TRADE));
    assertTrue(t.isCompatible(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION)));
    assertFalse(t.isCompatible(ComputationTargetType.POSITION.containing(ComputationTargetType.SECURITY)));
    assertTrue(t.isCompatible(ComputationTargetType.POSITION.or(ComputationTargetType.TRADE)));
    assertFalse(t.isCompatible(ComputationTargetType.PRIMITIVE.or(ComputationTargetType.SECURITY)));
  }

  public void testIsTargetType_null() {
    assertFalse(ComputationTargetType.NULL.isTargetType(Currency.USD.getClass()));
    assertTrue(ComputationTargetType.NULL.isTargetType(ComputationTargetType.NULL));
    assertFalse(ComputationTargetType.NULL.isTargetType(ComputationTargetType.PRIMITIVE));
    assertFalse(ComputationTargetType.NULL.isTargetType(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION)));
    assertFalse(ComputationTargetType.NULL.isTargetType(ComputationTargetType.POSITION.or(ComputationTargetType.TRADE)));
  }

  public void testIsTargetType_class() {
    assertFalse(ComputationTargetType.SECURITY.isTargetType(NODE.getClass()));
    assertFalse(ComputationTargetType.SECURITY.isTargetType(SECURITY.getClass()));
    assertTrue(ComputationTargetType.SECURITY.isTargetType(Security.class));
    assertFalse(ComputationTargetType.SECURITY.isTargetType(ComputationTargetType.NULL));
    assertFalse(ComputationTargetType.SECURITY.isTargetType(ComputationTargetType.POSITION));
    assertTrue(ComputationTargetType.SECURITY.isTargetType(ComputationTargetType.SECURITY));
    assertFalse(ComputationTargetType.SECURITY.isTargetType(ComputationTargetType.POSITION.containing(ComputationTargetType.SECURITY)));
    assertFalse(ComputationTargetType.SECURITY.isTargetType(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION)));
    assertTrue(ComputationTargetType.SECURITY.isTargetType(ComputationTargetType.PRIMITIVE.or(ComputationTargetType.SECURITY)));
    assertFalse(ComputationTargetType.SECURITY.isTargetType(ComputationTargetType.POSITION.or(ComputationTargetType.TRADE)));
  }

  public void testIsTargetType_nested() {
    final ComputationTargetType t = ComputationTargetType.POSITION.containing(ComputationTargetType.TRADE);
    assertFalse(t.isTargetType(TRADE.getClass()));
    assertTrue(t.isTargetType(Trade.class));
    assertFalse(t.isTargetType(POSITION.getClass()));
    assertFalse(t.isTargetType(ComputationTargetType.NULL));
    assertFalse(t.isTargetType(ComputationTargetType.POSITION));
    assertTrue(t.isTargetType(ComputationTargetType.TRADE));
    assertTrue(t.isTargetType(ComputationTargetType.POSITION.containing(ComputationTargetType.TRADE)));
    assertFalse(t.isTargetType(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION).containing(ComputationTargetType.TRADE)));
    assertFalse(t.isTargetType(ComputationTargetType.POSITION.containing(ComputationTargetType.TRADE).containing(ComputationTargetType.SECURITY)));
    assertTrue(t.containing(ComputationTargetType.SECURITY).isTargetType(ComputationTargetType.TRADE.containing(ComputationTargetType.SECURITY)));
    assertTrue(t.isTargetType(ComputationTargetType.POSITION.or(ComputationTargetType.TRADE)));
    assertFalse(t.isTargetType(ComputationTargetType.PRIMITIVE.or(ComputationTargetType.SECURITY)));
  }

  public void testIsTargetType_multiple() {
    final ComputationTargetType t = ComputationTargetType.POSITION.or(ComputationTargetType.TRADE);
    assertFalse(t.isTargetType(POSITION.getClass()));
    assertTrue(t.isTargetType(Position.class));
    assertFalse(t.isTargetType(TRADE.getClass()));
    assertTrue(t.isTargetType(Trade.class));
    assertFalse(t.isTargetType(SECURITY.getClass()));
    assertFalse(t.isTargetType(ComputationTargetType.NULL));
    assertFalse(t.isTargetType(ComputationTargetType.SECURITY));
    assertTrue(t.isTargetType(ComputationTargetType.TRADE));
    assertTrue(t.isTargetType(ComputationTargetType.POSITION));
    assertFalse(t.isTargetType(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION)));
    assertFalse(t.isTargetType(ComputationTargetType.POSITION.containing(ComputationTargetType.SECURITY)));
    assertTrue(t.isTargetType(ComputationTargetType.POSITION.or(ComputationTargetType.TRADE)));
    assertTrue(t.isTargetType(ComputationTargetType.POSITION.or(ComputationTargetType.SECURITY)));
    assertFalse(t.isTargetType(ComputationTargetType.PRIMITIVE.or(ComputationTargetType.SECURITY)));
  }

  private void testParseToString(final ComputationTargetType type, final String name1, String name2, String str1, String str2) {
    if (name2 == null) {
      name2 = name1;
    }
    if (str2 == null) {
      if (str1 == null) {
        str2 = name2;
      } else {
        str2 = str1;
      }
    }
    if (str1 == null) {
      str1 = name1;
    }
    String value = type.getName();
    if (!name1.equals(value)) {
      assertEquals(value, name2);
    }
    value = type.toString();
    if (!str1.equals(value)) {
      assertEquals(value, str2);
    }
    assertEquals(ComputationTargetType.parse(str1), type);
    assertEquals(ComputationTargetType.parse(str2), type);
  }

  private static class Foo implements UniqueIdentifiable {

    @Override
    public UniqueId getUniqueId() {
      return null;
    }

  }

  public void testParseToString() {
    testParseToString(ComputationTargetType.NULL, "NULL", null, null, null);
    testParseToString(ComputationTargetType.PORTFOLIO_NODE, "PORTFOLIO_NODE", null, null, null);
    testParseToString(ComputationTargetType.POSITION, "POSITION", null, null, null);
    testParseToString(ComputationTargetType.TRADE, "TRADE", null, null, null);
    testParseToString(ComputationTargetType.SECURITY, "SECURITY", null, null, null);
    testParseToString(ComputationTargetType.PRIMITIVE, "PRIMITIVE", null, null, null);
    testParseToString(ComputationTargetType.of(Currency.class), "CURRENCY", null, null, null);
    testParseToString(ComputationTargetType.of(Foo.class), "Foo", null, "com.opengamma.engine.target.ComputationTargetTypeTest$Foo", null);
    testParseToString(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION), "PORTFOLIO_NODE/POSITION", null, null, null);
    testParseToString(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION.or(ComputationTargetType.TRADE)).containing(ComputationTargetType.SECURITY),
        "PORTFOLIO_NODE/(POSITION|TRADE)/SECURITY", "PORTFOLIO_NODE/(TRADE|POSITION)/SECURITY", null, null);
    testParseToString(ComputationTargetType.POSITION.containing(ComputationTargetType.SECURITY).or(ComputationTargetType.of(Foo.class)), "(POSITION/SECURITY)|Foo",
        "Foo|(POSITION/SECURITY)", "(POSITION/SECURITY)|com.opengamma.engine.target.ComputationTargetTypeTest$Foo", "com.opengamma.engine.target.ComputationTargetTypeTest$Foo|(POSITION/SECURITY)");
  }

  public void testEquals_null() {
    assertTrue(ComputationTargetType.NULL.equals(ComputationTargetType.NULL));
    assertFalse(ComputationTargetType.NULL.equals(ComputationTargetType.POSITION));
    assertFalse(ComputationTargetType.NULL.equals(ComputationTargetType.POSITION.containing(ComputationTargetType.SECURITY)));
    assertFalse(ComputationTargetType.NULL.equals(ComputationTargetType.POSITION.or(ComputationTargetType.TRADE)));
    assertFalse(ComputationTargetType.NULL.equals(null));
  }

  public void testEquals_class() {
    assertFalse(ComputationTargetType.POSITION.equals(ComputationTargetType.NULL));
    assertTrue(ComputationTargetType.POSITION.equals(ComputationTargetType.POSITION));
    assertFalse(ComputationTargetType.POSITION.equals(ComputationTargetType.SECURITY));
    assertFalse(ComputationTargetType.POSITION.equals(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION)));
    assertFalse(ComputationTargetType.POSITION.equals(ComputationTargetType.POSITION.or(ComputationTargetType.TRADE)));
    assertFalse(ComputationTargetType.POSITION.equals(null));
  }

  public void testEquals_nested() {
    final ComputationTargetType t = ComputationTargetType.POSITION.containing(ComputationTargetType.SECURITY);
    assertFalse(t.equals(ComputationTargetType.NULL));
    assertFalse(t.equals(ComputationTargetType.POSITION));
    assertFalse(t.equals(ComputationTargetType.SECURITY));
    assertTrue(t.equals(ComputationTargetType.POSITION.containing(ComputationTargetType.SECURITY)));
    assertFalse(t.equals(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION)));
    assertFalse(t.equals(ComputationTargetType.POSITION.or(ComputationTargetType.TRADE)));
    assertFalse(t.equals(null));
  }

  public void testEquals_multiple() {
    final ComputationTargetType t = ComputationTargetType.POSITION.or(ComputationTargetType.TRADE);
    assertFalse(t.equals(ComputationTargetType.NULL));
    assertFalse(t.equals(ComputationTargetType.POSITION));
    assertFalse(t.equals(ComputationTargetType.TRADE));
    assertFalse(t.equals(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION)));
    assertTrue(t.equals(ComputationTargetType.TRADE.or(ComputationTargetType.POSITION)));
    assertFalse(t.equals(ComputationTargetType.POSITION.or(ComputationTargetType.SECURITY)));
    assertFalse(t.equals(null));
  }

  public void testConstruction_nested() {
    final ComputationTargetType t1 = ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION).containing(HistoricalTimeSeries.class);
    final ComputationTargetType t2 = ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION.containing(HistoricalTimeSeries.class));
    assertEquals(t1, t2);
  }

  public void testConstruction_multiple() {
    final ComputationTargetType t1 = ComputationTargetType.PORTFOLIO_NODE.or(ComputationTargetType.POSITION).or(HistoricalTimeSeries.class);
    final ComputationTargetType t2 = ComputationTargetType.PORTFOLIO_NODE.or(ComputationTargetType.POSITION.or(HistoricalTimeSeries.class));
    assertEquals(t1, t2);
    final ComputationTargetType t3 = ComputationTargetType.multiple(ComputationTargetType.PORTFOLIO_NODE, ComputationTargetType.POSITION, ComputationTargetType.of(HistoricalTimeSeries.class));
    assertEquals(t1, t3);
  }

}
