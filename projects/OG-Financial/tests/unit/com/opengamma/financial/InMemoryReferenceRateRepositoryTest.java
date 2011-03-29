/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import org.testng.annotations.Test;
import org.testng.AssertJUnit;
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
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Unit test for InMemoryReferenceRateRepository
 */
public class InMemoryReferenceRateRepositoryTest {

  @Test
  public void testRepostiory() {
    final ConventionBundleMaster repo = new InMemoryConventionBundleMaster();
    final ConventionBundleSource source = new DefaultConventionBundleSource(repo);
    final ConventionBundle conventions = source.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR O/N"));
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount actact = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    AssertJUnit.assertEquals("USD LIBOR O/N", conventions.getName());
    AssertJUnit.assertEquals(IdentifierBundle.of(SecurityUtils.bloombergTickerSecurityId("US00O/N Index"), Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR O/N")), conventions
        .getIdentifiers());
    AssertJUnit.assertEquals(UniqueIdentifier.of(InMemoryConventionBundleMaster.IN_MEMORY_UNIQUE_SCHEME.getName(), "1"), conventions.getUniqueId());
    AssertJUnit.assertEquals(actact, conventions.getDayCount());
    AssertJUnit.assertEquals(following, conventions.getBusinessDayConvention());
    AssertJUnit.assertEquals(0, conventions.getSettlementDays());

    final ConventionBundle conventions2 = source.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 3m"));
    AssertJUnit.assertEquals("USD LIBOR 3m", conventions2.getName());
    AssertJUnit.assertEquals(IdentifierBundle.of(SecurityUtils.bloombergTickerSecurityId("US0003M Index"), Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 3m")), conventions2
        .getIdentifiers());
    AssertJUnit.assertEquals(UniqueIdentifier.of(InMemoryConventionBundleMaster.IN_MEMORY_UNIQUE_SCHEME.getName(), "7"), conventions2.getUniqueId());
    AssertJUnit.assertEquals(actact, conventions2.getDayCount());
    AssertJUnit.assertEquals(modified, conventions2.getBusinessDayConvention());
    AssertJUnit.assertEquals(2, conventions2.getSettlementDays());
  }
}
