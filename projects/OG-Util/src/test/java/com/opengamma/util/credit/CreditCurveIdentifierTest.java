/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.credit;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
@SuppressWarnings("deprecation")
public class CreditCurveIdentifierTest {

  private static final String RED_CODE = "ABC";
  private static final Currency CURRENCY = Currency.of("USD");
  private static final String TERM = "1Y";
  private static final String SENIORITY = "SENIOR";
  private static final String RESTRUCTURING_CLAUSE = "NONE";

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIssuer() {
    CreditCurveIdentifier.of((String) null, CURRENCY, TERM, SENIORITY, RESTRUCTURING_CLAUSE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSeniority() {
    CreditCurveIdentifier.of(RED_CODE, CURRENCY, TERM, null, RESTRUCTURING_CLAUSE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRestructuringClause() {
    CreditCurveIdentifier.of(RED_CODE, CURRENCY, TERM, SENIORITY, null);
  }

  @Test
  public void testEqualsHashCode() {
    final CreditCurveIdentifier id = CreditCurveIdentifier.of(RED_CODE, CURRENCY, TERM, SENIORITY, RESTRUCTURING_CLAUSE);
    assertEquals(id.getRedCode(), RED_CODE);
    assertEquals(id.getCurrency(), CURRENCY);
    assertEquals(id.getTerm(), TERM);
    assertEquals(id.getSeniority(), SENIORITY);
    assertEquals(id.getRestructuringClause(), RESTRUCTURING_CLAUSE);
    CreditCurveIdentifier other = CreditCurveIdentifier.of(RED_CODE, CURRENCY, TERM, SENIORITY, RESTRUCTURING_CLAUSE);
    assertEquals(id, other);
    assertEquals(id.hashCode(), other.hashCode());
    assertFalse(id.equals(null));
    assertFalse(id.equals(UnorderedCurrencyPair.of(Currency.AUD, Currency.CAD)));
    other = CreditCurveIdentifier.of(ExternalId.of("Scheme", "DEF"), CURRENCY, TERM, SENIORITY, RESTRUCTURING_CLAUSE);
    assertFalse(other.equals(id));
    other = CreditCurveIdentifier.of(RED_CODE, CURRENCY, TERM, RESTRUCTURING_CLAUSE, RESTRUCTURING_CLAUSE);
    assertFalse(other.equals(id));
    other = CreditCurveIdentifier.of(RED_CODE, CURRENCY, TERM, SENIORITY, SENIORITY);
    assertFalse(other.equals(id));
  }

}
