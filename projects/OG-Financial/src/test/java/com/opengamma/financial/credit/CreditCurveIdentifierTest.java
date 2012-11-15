/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.credit;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class CreditCurveIdentifierTest {
  private static final String ISSUER_ID = "ABC";
  private static final String SENIORITY = "SENIOR";
  private static final String RESTRUCTURING_CLAUSE = "NONE";

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIssuer() {
    CreditCurveIdentifier.of((String) null, SENIORITY, RESTRUCTURING_CLAUSE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSeniority() {
    CreditCurveIdentifier.of(ISSUER_ID, null, RESTRUCTURING_CLAUSE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRestructuringClause() {
    CreditCurveIdentifier.of(ISSUER_ID, SENIORITY, null);
  }

  @Test
  public void testEqualsHashCode() {
    final CreditCurveIdentifier id = CreditCurveIdentifier.of(ISSUER_ID, SENIORITY, RESTRUCTURING_CLAUSE);
    assertEquals(id.getIssuer(), ISSUER_ID);
    assertEquals(id.getSeniority(), SENIORITY);
    assertEquals(id.getRestructuringClause(), RESTRUCTURING_CLAUSE);
    CreditCurveIdentifier other = CreditCurveIdentifier.of(ISSUER_ID, SENIORITY, RESTRUCTURING_CLAUSE);
    assertEquals(id, other);
    assertEquals(id.hashCode(), other.hashCode());
    assertFalse(id.equals(null));
    assertFalse(id.equals(UnorderedCurrencyPair.of(Currency.AUD, Currency.CAD)));
    other = CreditCurveIdentifier.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DEF"), SENIORITY, RESTRUCTURING_CLAUSE);
    assertFalse(other.equals(id));
    other = CreditCurveIdentifier.of(ISSUER_ID, RESTRUCTURING_CLAUSE, RESTRUCTURING_CLAUSE);
    assertFalse(other.equals(id));
    other = CreditCurveIdentifier.of(ISSUER_ID, SENIORITY, SENIORITY);
    assertFalse(other.equals(id));
  }

}
