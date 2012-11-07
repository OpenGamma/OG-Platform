/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.commodity.derivative.EnergyForward;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.DateUtils;

/**
 *
 */
public class EnergyForwardDefinitionTest {

  private final static ExternalId AN_UNDERLYING= ExternalId.of("Scheme", "value");
  private final static ZonedDateTime FIRST_DELIVERY_DATE = DateUtils.getUTCDate(2011, 9, 21);
  private final static ZonedDateTime LAST_DELIVERY_DATE = DateUtils.getUTCDate(2012, 9, 21);
  private final static ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2011, 9, 21);
  private final static ZonedDateTime A_DATE = DateUtils.getUTCDate(2011, 9, 20);

  /**
   * Test delivery dates not allowed for CASH delivery type
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void cashHasNoDelivery() {
    new EnergyForwardDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, FIRST_DELIVERY_DATE, LAST_DELIVERY_DATE, 1000, "tonnes", SettlementType.CASH);
  }

  /**
   * Test invalid delivery dates for physical delivery type
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void physicalHasDelivery() {
    new EnergyForwardDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, FIRST_DELIVERY_DATE, null, 1000, "tonnes", SettlementType.PHYSICAL);
  }

  /**
   * Test hashCode and equals methods.
   */
  @Test()
  public void testHashEquals() {
    EnergyForwardDefinition first = new EnergyForwardDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, FIRST_DELIVERY_DATE, LAST_DELIVERY_DATE, 1000, "tonnes", SettlementType.PHYSICAL);
    EnergyForwardDefinition second = new EnergyForwardDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, FIRST_DELIVERY_DATE, LAST_DELIVERY_DATE, 1000, "tonnes", SettlementType.PHYSICAL);
    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
    EnergyForwardDefinition third = new EnergyForwardDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, null, null, 1000, "tonnes", SettlementType.CASH);
    assertFalse(first.equals(third));
    assertFalse(second.hashCode() == third.hashCode());
  }

  /**
   * Test builder methods.
   */
  @Test
  public void testBuilders() {
    EnergyForwardDefinition cash = EnergyForwardDefinition.withCashSettlement(EXPIRY_DATE, AN_UNDERLYING, 100, 1000, "tonnes");
    EnergyForwardDefinition physical = EnergyForwardDefinition.withPhysicalSettlement(EXPIRY_DATE, AN_UNDERLYING, 100, FIRST_DELIVERY_DATE, LAST_DELIVERY_DATE, 1000, "tonnes");
    EnergyForwardDefinition first = new EnergyForwardDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, null, null, 1000, "tonnes", SettlementType.CASH);
    EnergyForwardDefinition second = new EnergyForwardDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, FIRST_DELIVERY_DATE, LAST_DELIVERY_DATE, 1000, "tonnes", SettlementType.PHYSICAL);
    assertEquals(cash, first);
    assertEquals(physical, second);
  }

  /**
   * Test getters
   */
  @Test
  public void testGetters() {
    EnergyForwardDefinition first = new EnergyForwardDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, null, null, 1000., "tonnes", SettlementType.CASH);
    EnergyForwardDefinition second = new EnergyForwardDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, FIRST_DELIVERY_DATE, LAST_DELIVERY_DATE, 1000., "tonnes", SettlementType.PHYSICAL);
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
   * Test method for {@link com.opengamma.analytics.financial.commodity.definition.EnergyForwardDefinition#toDerivative(javax.time.calendar.ZonedDateTime)}.
   */
  @Test
  public void testToDerivative() {
    EnergyForwardDefinition first = new EnergyForwardDefinition(EXPIRY_DATE, AN_UNDERLYING, 100., null, null, 1000., "tonnes", SettlementType.CASH);
    EnergyForwardDefinition second = new EnergyForwardDefinition(EXPIRY_DATE, AN_UNDERLYING, 100., FIRST_DELIVERY_DATE, LAST_DELIVERY_DATE, 1000., "tonnes", SettlementType.PHYSICAL);

    EnergyForward firstDerivative = first.toDerivative(A_DATE);
    EnergyForward secondDerivative = second.toDerivative(A_DATE);
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

    EnergyForward firstDerivative2 = new EnergyForward(0.0027397260273972603, AN_UNDERLYING, 100, null, null, 1000, "tonnes", SettlementType.CASH);
    assertEquals(firstDerivative.hashCode(), firstDerivative2.hashCode());
    assertEquals(firstDerivative, firstDerivative2);
  }

}
