/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.CompoundingType;
import com.opengamma.financial.analytics.fudgemsg.AnalyticsTestBase;
import com.opengamma.financial.convention.CMSLegConvention;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class ConventionBuildersTest extends AnalyticsTestBase {

  @Test
  public void testCMSLegConvention() {
    final CMSLegConvention convention = new CMSLegConvention("EUR CMS", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR CMS")),
        ExternalId.of("Test", "EUR 6m Swap Index"), Tenor.SIX_MONTHS, true);
    convention.setUniqueId(UniqueId.of("Test", "123"));
    assertEquals(convention, cycleObject(CMSLegConvention.class, convention));
  }

  @Test
  public void testCompoundingIborLegConvention() {
    final CompoundingIborLegConvention convention = new CompoundingIborLegConvention("EUR CMS", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR CMS")),
        ExternalId.of("Test", "EUR 6m Swap Index"), Tenor.SIX_MONTHS, CompoundingType.FLAT_COMPOUNDING);
    convention.setUniqueId(UniqueId.of("Test", "12345"));
    assertEquals(convention, cycleObject(CompoundingIborLegConvention.class, convention));
  }

  @Test
  public void testDepositConvention() {
    final DepositConvention convention = new DepositConvention("EUR Deposit", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR Deposit")),
        DayCountFactory.INSTANCE.getDayCount("Act/365"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), 2, true,
        Currency.EUR, ExternalId.of("Test", "EU"));
    convention.setUniqueId(UniqueId.of("Test", "1234"));
    assertEquals(convention, cycleObject(DepositConvention.class, convention));
  }

}
