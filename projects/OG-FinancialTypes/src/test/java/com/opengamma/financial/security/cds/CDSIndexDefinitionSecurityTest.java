/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.cds;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CDSIndexDefinitionSecurityTest extends AbstractFudgeBuilderTestCase {

  private static final CreditDefaultSwapIndexDefinitionSecurity s_cdsIndexDefnSecurity;
  static {
    CreditDefaultSwapIndexComponent component1 = new CreditDefaultSwapIndexComponent("A", ExternalSchemes.markItRedCode("SZRTY"), 10.5, ExternalSchemes.isinSecurityId("ABC3456"));
    CreditDefaultSwapIndexComponent component2 = new CreditDefaultSwapIndexComponent("B", ExternalSchemes.markItRedCode("ERT234"), 5.7, ExternalSchemes.isinSecurityId("ABC7890"));
    CDSIndexComponentBundle components = CDSIndexComponentBundle.of(component1, component2);
    CreditDefaultSwapIndexDefinitionSecurity security = new CreditDefaultSwapIndexDefinitionSecurity("1", "5", "CDX", Currency.USD, 0.4,
        CDSIndexTerms.of(Tenor.ONE_WEEK, Tenor.ONE_YEAR),
        components);
    security.setName("TEST_CDSINDEX_SEC");
    security.addExternalId(ExternalSchemes.markItRedCode("CDXI234"));
    s_cdsIndexDefnSecurity = security;
  }

  @Test
  public void testCycle() {
    assertEquals(s_cdsIndexDefnSecurity, cycleObject(CreditDefaultSwapIndexDefinitionSecurity.class,
        s_cdsIndexDefnSecurity));
  }

}
