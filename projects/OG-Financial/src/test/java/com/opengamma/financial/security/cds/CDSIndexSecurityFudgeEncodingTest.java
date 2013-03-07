/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.cds;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.fudgemsg.FinancialTestBase;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Test {@link CreditDefaultSwapIndexSecurity} Fudge support.
 */
public class CDSIndexSecurityFudgeEncodingTest extends FinancialTestBase {

  private static final CreditDefaultSwapIndexSecurity s_cdsIndexSecurity;
  static {
    CreditDefaultSwapIndexComponent component1 = new CreditDefaultSwapIndexComponent("A", ExternalSchemes.redCode("SZRTY"), 10.5, ExternalSchemes.isinSecurityId("ABC3456"));
    CreditDefaultSwapIndexComponent component2 = new CreditDefaultSwapIndexComponent("B", ExternalSchemes.redCode("ERT234"), 5.7, ExternalSchemes.isinSecurityId("ABC7890"));
    CDSIndexComponentBundle components = CDSIndexComponentBundle.of(component1, component2);
    CreditDefaultSwapIndexSecurity security = new CreditDefaultSwapIndexSecurity("1", "5", "CDX", Currency.USD, 
        CDSIndexTerms.of(Tenor.ONE_WEEK, Tenor.ONE_YEAR), 
        components );
    security.setName("TEST_CDSINDEX_SEC");
    security.addExternalId(ExternalSchemes.redCode("CDXI234"));
    s_cdsIndexSecurity = security;
  }

  @Test
  public void testCycle() {
    assertEquals(s_cdsIndexSecurity, cycleObject(CreditDefaultSwapIndexSecurity.class, s_cdsIndexSecurity));
  }

}
