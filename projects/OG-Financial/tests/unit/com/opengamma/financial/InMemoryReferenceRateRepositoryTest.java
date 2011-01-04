/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import junit.framework.Assert;

import org.junit.Test;

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
    Assert.assertEquals("USD LIBOR O/N", conventions.getName());
    Assert.assertEquals(IdentifierBundle.of(SecurityUtils.bloombergTickerSecurityId("US00O/N Index"), Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR O/N")), conventions
        .getIdentifiers());
    Assert.assertEquals(UniqueIdentifier.of(InMemoryConventionBundleMaster.IN_MEMORY_UNIQUE_SCHEME.getName(), "1"), conventions.getUniqueId());
    Assert.assertEquals(actact, conventions.getDayCount());
    Assert.assertEquals(following, conventions.getBusinessDayConvention());
    Assert.assertEquals(0, conventions.getSettlementDays());

    final ConventionBundle conventions2 = source.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 3m"));
    Assert.assertEquals("USD LIBOR 3m", conventions2.getName());
    Assert.assertEquals(IdentifierBundle.of(SecurityUtils.bloombergTickerSecurityId("US0003M Index"), Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 3m")), conventions2
        .getIdentifiers());
    Assert.assertEquals(UniqueIdentifier.of(InMemoryConventionBundleMaster.IN_MEMORY_UNIQUE_SCHEME.getName(), "7"), conventions2.getUniqueId());
    Assert.assertEquals(actact, conventions2.getDayCount());
    Assert.assertEquals(following, conventions2.getBusinessDayConvention());
    Assert.assertEquals(2, conventions2.getSettlementDays());
  }
}
