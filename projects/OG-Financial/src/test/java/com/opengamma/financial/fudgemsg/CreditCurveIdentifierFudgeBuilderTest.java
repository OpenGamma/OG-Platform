/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.credit.CreditCurveIdentifier;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class CreditCurveIdentifierFudgeBuilderTest extends FinancialTestBase {

  @Test
  public void test() {
    final ExternalId redCodeId = ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "ABC");
    final String seniority = "SENIOR";
    final String restructuringClause = "NONE";
    final Currency currency = Currency.of("USD");
    final String term = "1Y";
    final CreditCurveIdentifier id = CreditCurveIdentifier.of(redCodeId, currency, term, seniority, restructuringClause);
    assertEquals(id, cycleObject(CreditCurveIdentifier.class, id));
  }

}
