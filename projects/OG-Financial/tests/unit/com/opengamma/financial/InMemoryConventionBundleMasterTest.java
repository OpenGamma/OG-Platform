/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleMaster;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;

/**
 * Unit test for InMemoryConventionBundleMaster.
 */
public class InMemoryConventionBundleMasterTest {
  private static final int OFFSET = 572;

  @Test
  public void testRepository() {
    final ConventionBundleMaster repo = new InMemoryConventionBundleMaster();
    final ConventionBundleSource source = new DefaultConventionBundleSource(repo);
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount actact = DayCountFactory.INSTANCE.getDayCount("Actual/360");

    final ConventionBundle conventions = source.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR O/N"));
    AssertJUnit.assertEquals("USD LIBOR O/N", conventions.getName());
    AssertJUnit.assertEquals("US00O/N Index", conventions.getIdentifiers().getValue(SecurityUtils.BLOOMBERG_TICKER));
    AssertJUnit.assertEquals("USD LIBOR O/N", conventions.getIdentifiers().getValue(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME));
    AssertJUnit.assertEquals(UniqueId.of(InMemoryConventionBundleMaster.IN_MEMORY_UNIQUE_SCHEME.getName(), String.valueOf(OFFSET)), conventions.getUniqueId());
    AssertJUnit.assertEquals(actact, conventions.getDayCount());
    AssertJUnit.assertEquals(following, conventions.getBusinessDayConvention());
    AssertJUnit.assertEquals(0, conventions.getSettlementDays());

    final ConventionBundle conventions2 = source.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 3m"));
    AssertJUnit.assertEquals("USD LIBOR 3m", conventions2.getName());
    AssertJUnit.assertEquals("US0003M Index", conventions2.getIdentifiers().getValue(SecurityUtils.BLOOMBERG_TICKER));
    AssertJUnit.assertEquals("USD LIBOR 3m", conventions2.getIdentifiers().getValue(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME));
    AssertJUnit.assertEquals("USDLIBORP3M", conventions2.getIdentifiers().getValue(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER));
    AssertJUnit.assertEquals(UniqueId.of(InMemoryConventionBundleMaster.IN_MEMORY_UNIQUE_SCHEME.getName(), String.valueOf(OFFSET + 6)), conventions2.getUniqueId());
    AssertJUnit.assertEquals(actact, conventions2.getDayCount());
    AssertJUnit.assertEquals(modified, conventions2.getBusinessDayConvention());
    AssertJUnit.assertEquals(2, conventions2.getSettlementDays());
  }

}
