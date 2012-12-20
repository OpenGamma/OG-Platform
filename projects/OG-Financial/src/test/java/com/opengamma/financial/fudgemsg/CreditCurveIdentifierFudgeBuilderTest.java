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

/**
 *
 */
public class CreditCurveIdentifierFudgeBuilderTest extends FinancialTestBase {

  @Test
  public void test() {
    final ExternalId issuerId = ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "ABC");
    final String seniority = "SENIOR";
    final String restructuringClause = "NONE";
    final CreditCurveIdentifier id = CreditCurveIdentifier.of(issuerId, seniority, restructuringClause);
    assertEquals(id, cycleObject(CreditCurveIdentifier.class, id));
  }

}
