package com.opengamma.analytics.financial.forex.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ForexNonDeliverableOptionDefinitionTest {

  private static final Currency KRW = Currency.of("KRW");
  private static final Currency USD = Currency.EUR;
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2012, 5, 2);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2012, 5, 4);
  private static final double NOMINAL_USD = 1000000; // 1m
  private static final double STRIKE_USD_KRW = 1123.45;
  private static final ForexNonDeliverableForwardDefinition NDF_DEFINITION = new ForexNonDeliverableForwardDefinition(KRW, USD, NOMINAL_USD, STRIKE_USD_KRW, FIXING_DATE, PAYMENT_DATE);

  private static final boolean IS_CALL = true;
  private static final boolean IS_LONG = true;
  private static final ForexNonDeliverableOptionDefinition NDO_DEFINITION = new ForexNonDeliverableOptionDefinition(NDF_DEFINITION, IS_CALL, IS_LONG);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 11, 10);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFX() {
    new ForexNonDeliverableOptionDefinition(null, IS_CALL, IS_LONG);
  }

  @Test
  public void getter() {
    assertEquals("Forex non-deliverable option - getter", NDF_DEFINITION, NDO_DEFINITION.getUnderlyingNDF());
    assertEquals("Forex non-deliverable option - getter", IS_CALL, NDO_DEFINITION.isCall());
    assertEquals("Forex non-deliverable option - getter", IS_LONG, NDO_DEFINITION.isLong());
  }

  @Test
  /**
   * Tests the class toDerivative method.
   */
  public void toDerivative() {
    final ForexNonDeliverableOption ndoConverted = NDO_DEFINITION.toDerivative(REFERENCE_DATE);
    final ForexNonDeliverableOption ndoExpected = new ForexNonDeliverableOption(NDF_DEFINITION.toDerivative(REFERENCE_DATE), IS_CALL, IS_LONG);
    assertEquals("Forex NDO - toDerivatives", ndoExpected, ndoConverted);
  }

  @Test
  /**
   * Tests the class equal and hashCode
   */
  public void equalHash() {
    assertEquals("ForexNonDeliverableOptionDefinition: equal/hash code", NDO_DEFINITION, NDO_DEFINITION);
    assertFalse("ForexNonDeliverableOptionDefinition: equal/hash code", NDO_DEFINITION.equals("A"));
    assertFalse("ForexNonDeliverableOptionDefinition: equal/hash code", NDO_DEFINITION.equals(null));
    final ForexNonDeliverableOptionDefinition newNdo = new ForexNonDeliverableOptionDefinition(NDF_DEFINITION, IS_CALL, IS_LONG);
    assertTrue("ForexNonDeliverableOptionDefinition: equal/hash code", NDO_DEFINITION.equals(newNdo));
    assertTrue("ForexNonDeliverableOptionDefinition: equal/hash code", NDO_DEFINITION.hashCode() == newNdo.hashCode());
    final ForexNonDeliverableOptionDefinition newNdo2 = new ForexNonDeliverableOptionDefinition(NDF_DEFINITION, !IS_CALL, !IS_LONG);
    final ForexNonDeliverableOptionDefinition newNdo3 = new ForexNonDeliverableOptionDefinition(NDF_DEFINITION, !IS_CALL, !IS_LONG);
    assertEquals("ForexNonDeliverableOptionDefinition: equal/hash code", newNdo2.hashCode(), newNdo3.hashCode());
    ForexNonDeliverableOptionDefinition modifiedNdo;
    modifiedNdo = new ForexNonDeliverableOptionDefinition(new ForexNonDeliverableForwardDefinition(KRW, USD, NOMINAL_USD, STRIKE_USD_KRW + 1.0, FIXING_DATE, PAYMENT_DATE), IS_CALL, IS_LONG);
    assertFalse("Forex NDF: equal - hash code", NDO_DEFINITION.equals(modifiedNdo));
    modifiedNdo = new ForexNonDeliverableOptionDefinition(NDF_DEFINITION, !IS_CALL, IS_LONG);
    assertFalse("Forex NDF: equal - hash code", NDO_DEFINITION.equals(modifiedNdo));
    modifiedNdo = new ForexNonDeliverableOptionDefinition(NDF_DEFINITION, IS_CALL, !IS_LONG);
    assertFalse("Forex NDF: equal - hash code", NDO_DEFINITION.equals(modifiedNdo));
    assertFalse(NDF_DEFINITION.equals(USD));
    assertFalse(NDF_DEFINITION.equals(null));
  }

}
