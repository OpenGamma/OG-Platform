/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import static org.testng.Assert.assertFalse;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleMaster;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Unit test for InMemoryConventionBundleMaster.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryConventionBundleMasterTest {

  @Test
  public void testRepository() {
    final ConventionBundleMaster repo = new InMemoryConventionBundleMaster();
    final ConventionBundleSource source = new DefaultConventionBundleSource(repo);
    final BusinessDayConvention modified = BusinessDayConventions.MODIFIED_FOLLOWING;
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount actact = DayCounts.ACT_360;

    final ConventionBundle conventions = source.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR O/N"));
    AssertJUnit.assertEquals("USD LIBOR O/N", conventions.getName());
    AssertJUnit.assertEquals("US00O/N Index", conventions.getIdentifiers().getValue(ExternalSchemes.BLOOMBERG_TICKER));
    AssertJUnit.assertEquals("USD LIBOR O/N", conventions.getIdentifiers().getValue(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME));
    final UniqueId uidON = conventions.getUniqueId();
    AssertJUnit.assertEquals(InMemoryConventionBundleMaster.IN_MEMORY_UNIQUE_SCHEME.getName(), uidON.getScheme());
    AssertJUnit.assertEquals(actact, conventions.getDayCount());
    AssertJUnit.assertEquals(following, conventions.getBusinessDayConvention());
    AssertJUnit.assertEquals(0, (int) conventions.getSettlementDays());

    final ConventionBundle conventions2 = source.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 3m"));
    AssertJUnit.assertEquals("USD LIBOR 3m", conventions2.getName());
    AssertJUnit.assertEquals("US0003M Index", conventions2.getIdentifiers().getValue(ExternalSchemes.BLOOMBERG_TICKER));
    AssertJUnit.assertEquals("USD LIBOR 3m", conventions2.getIdentifiers().getValue(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME));
    final UniqueId uid3M = conventions2.getUniqueId ();
    AssertJUnit.assertEquals(InMemoryConventionBundleMaster.IN_MEMORY_UNIQUE_SCHEME.getName(), uid3M.getScheme ());
    AssertJUnit.assertEquals(actact, conventions2.getDayCount());
    AssertJUnit.assertEquals(modified, conventions2.getBusinessDayConvention());
    AssertJUnit.assertEquals(2, (int) conventions2.getSettlementDays());

    assertFalse(uidON.equals (uid3M));

  }

}
