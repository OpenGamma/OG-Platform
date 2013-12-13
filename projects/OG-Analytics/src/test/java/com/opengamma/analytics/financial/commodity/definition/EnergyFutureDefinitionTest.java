/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.derivative.EnergyFuture;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EnergyFutureDefinitionTest {

  private final static ExternalId AN_UNDERLYING= ExternalId.of("Scheme", "value");
  private final static ZonedDateTime FIRST_DELIVERY_DATE = DateUtils.getUTCDate(2011, 9, 21);
  private final static ZonedDateTime LAST_DELIVERY_DATE = DateUtils.getUTCDate(2012, 9, 21);
  private final static ZonedDateTime SETTLEMENT_DATE = LAST_DELIVERY_DATE;
  private final static ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2011, 9, 21);
  private final static ZonedDateTime A_DATE = DateUtils.getUTCDate(2011, 9, 20);

  /**
   * Test delivery dates not allowed for CASH delivery type
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void cashHasNoDelivery() {
    new EnergyFutureDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, FIRST_DELIVERY_DATE, LAST_DELIVERY_DATE, 1000, "tonnes", SettlementType.CASH, 0, Currency.GBP, SETTLEMENT_DATE);
  }

  /**
   * Test invalid delivery dates for physical delivery type
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void physicalHasDelivery() {
    new EnergyFutureDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, FIRST_DELIVERY_DATE, null, 1000, "tonnes", SettlementType.PHYSICAL, 0, Currency.GBP, SETTLEMENT_DATE);
  }

  /**
   * Test hashCode and equals methods.
   */
  @Test()
  public void testHashEquals() {
    EnergyFutureDefinition first = new EnergyFutureDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, FIRST_DELIVERY_DATE, LAST_DELIVERY_DATE, 1000, "tonnes", SettlementType.PHYSICAL, 0, Currency.GBP,
        SETTLEMENT_DATE);
    EnergyFutureDefinition second = new EnergyFutureDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, FIRST_DELIVERY_DATE, LAST_DELIVERY_DATE, 1000, "tonnes", SettlementType.PHYSICAL, 0, Currency.GBP,
        SETTLEMENT_DATE);
    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
    EnergyFutureDefinition third = new EnergyFutureDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, null, null, 1000, "tonnes", SettlementType.CASH, 0, Currency.GBP, SETTLEMENT_DATE);
    assertFalse(first.equals(third));
    assertFalse(second.hashCode() == third.hashCode());
  }

  /**
   * Test builder methods.
   */
  @Test
  public void testBuilders() {
    EnergyFutureDefinition cash = EnergyFutureDefinition.withCashSettlement(EXPIRY_DATE, AN_UNDERLYING, 100, 1000, "tonnes", 0, Currency.GBP, SETTLEMENT_DATE);
    EnergyFutureDefinition physical = EnergyFutureDefinition.withPhysicalSettlement(EXPIRY_DATE, AN_UNDERLYING, 100, FIRST_DELIVERY_DATE, LAST_DELIVERY_DATE, 1000, "tonnes", 0, Currency.GBP,
        SETTLEMENT_DATE);
    EnergyFutureDefinition first = new EnergyFutureDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, null, null, 1000, "tonnes", SettlementType.CASH, 0, Currency.GBP, SETTLEMENT_DATE);
    EnergyFutureDefinition second = new EnergyFutureDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, FIRST_DELIVERY_DATE, LAST_DELIVERY_DATE, 1000, "tonnes", SettlementType.PHYSICAL, 0, Currency.GBP,
        SETTLEMENT_DATE);
    assertEquals(cash, first);
    assertEquals(physical, second);
  }

  /**
   * Test getters
   */
  @Test
  public void testGetters() {
    EnergyFutureDefinition first = new EnergyFutureDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, null, null, 1000., "tonnes", SettlementType.CASH, 0, Currency.GBP, SETTLEMENT_DATE);
    EnergyFutureDefinition second = new EnergyFutureDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, FIRST_DELIVERY_DATE, LAST_DELIVERY_DATE, 1000., "tonnes", SettlementType.PHYSICAL, 0, Currency.GBP,
        SETTLEMENT_DATE);
    assertEquals(first.getAmount(), 1000.);
    assertEquals(first.getUnitAmount(), 100.);
    assertEquals(first.getUnitName(), "tonnes");
    assertEquals(first.getExpiryDate(), EXPIRY_DATE);
    assertNull(first.getFirstDeliveryDate());
    assertNull(first.getLastDeliveryDate());
    assertEquals(first.getSettlementType(), SettlementType.CASH);
    assertEquals(first.getUnderlying(), AN_UNDERLYING);
    assertEquals(second.getFirstDeliveryDate(), FIRST_DELIVERY_DATE);
    assertEquals(second.getLastDeliveryDate(), LAST_DELIVERY_DATE);
    assertEquals(second.getSettlementType(), SettlementType.PHYSICAL);
  }

  /**
   * Test method for {@link com.opengamma.analytics.financial.commodity.definition.EnergyFutureDefinition#toDerivative(javax.time.calendar.ZonedDateTime)}.
   */
  @Test
  public void testToDerivative() {
    EnergyFutureDefinition first = new EnergyFutureDefinition(EXPIRY_DATE, AN_UNDERLYING, 100., null, null, 1000., "tonnes", SettlementType.CASH, 0, Currency.GBP, SETTLEMENT_DATE);
    EnergyFutureDefinition second = new EnergyFutureDefinition(EXPIRY_DATE, AN_UNDERLYING, 100., FIRST_DELIVERY_DATE, LAST_DELIVERY_DATE, 1000., "tonnes", SettlementType.PHYSICAL, 0, Currency.GBP,
        SETTLEMENT_DATE);

    EnergyFuture firstDerivative = first.toDerivative(A_DATE);
    EnergyFuture secondDerivative = second.toDerivative(A_DATE);
    assertEquals(firstDerivative.getAmount(), 1000.);
    assertEquals(firstDerivative.getUnitAmount(), 100.);
    assertEquals(firstDerivative.getUnitName(), "tonnes");
    assertEquals(firstDerivative.getExpiry(), 0.0027397260273972603);
    assertNull(firstDerivative.getFirstDeliveryDate());
    assertNull(firstDerivative.getLastDeliveryDate());
    assertEquals(firstDerivative.getSettlementType(), SettlementType.CASH);
    assertEquals(firstDerivative.getUnderlying(), AN_UNDERLYING);
    assertEquals(secondDerivative.getFirstDeliveryDate(), FIRST_DELIVERY_DATE);
    assertEquals(secondDerivative.getLastDeliveryDate(), LAST_DELIVERY_DATE);
    assertEquals(secondDerivative.getSettlementType(), SettlementType.PHYSICAL);

    EnergyFuture firstDerivative2 = new EnergyFuture(0.0027397260273972603, AN_UNDERLYING, 100, null, null, 1000, "tonnes", SettlementType.CASH, 1.0035032562317538, 0, Currency.GBP);
    assertEquals(firstDerivative.hashCode(), firstDerivative2.hashCode());
    assertEquals(firstDerivative, firstDerivative2);
  }

}
