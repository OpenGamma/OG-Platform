/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SecurityPersisterTest {
  
  private static class Impl extends SecurityPersister {

    private ManageableSecurity _security;

    @Override
    protected void storeSecurityImpl(final ManageableSecurity security) {
      _security = security;
      security.setUniqueId(UniqueId.of("Foo", "Bar"));
    }

  }

  public void testDuplicateSecurities() {
    final Impl impl = new Impl();
    ZonedDateTime now = ZonedDateTime.now();
    final EquityOptionSecurity security1 =
        new EquityOptionSecurity(OptionType.CALL, 1.0, Currency.USD, ExternalId.of("S", "V"),
                                 new AmericanExerciseType(), new Expiry(now), 1.0, "EXCH");
    final EquityOptionSecurity security2 =
        new EquityOptionSecurity(OptionType.CALL, 1.0, Currency.USD, ExternalId.of("S", "V"),
                                 new AmericanExerciseType(), new Expiry(now), 1.0, "EXCH");
    final ExternalIdBundle identifiers1 = impl.storeSecurity(security1);
    assertSame(impl._security, security1);
    assertEquals(security1.getExternalIdBundle(), identifiers1);
    impl._security = null;
    final ExternalIdBundle identifiers2 = impl.storeSecurity(security2);
    assertNull(impl._security);
    assertSame(identifiers2, identifiers1);
  }

  public void testDifferentSecurities() {
    final Impl impl = new Impl();
    final EquityOptionSecurity security1 = new EquityOptionSecurity(OptionType.CALL, 1.0, Currency.USD, ExternalId.of("S", "V1"), new AmericanExerciseType(), new Expiry(ZonedDateTime.now()), 1.0,
        "EXCH");
    final EquityOptionSecurity security2 = new EquityOptionSecurity(OptionType.CALL, 1.0, Currency.USD, ExternalId.of("S", "V2"), new AmericanExerciseType(), new Expiry(ZonedDateTime.now()), 1.0,
        "EXCH");
    final ExternalIdBundle identifiers1 = impl.storeSecurity(security1);
    assertSame(impl._security, security1);
    assertEquals(security1.getExternalIdBundle(), identifiers1);
    impl._security = null;
    final ExternalIdBundle identifiers2 = impl.storeSecurity(security2);
    assertSame(impl._security, security2);
    assertEquals(security2.getExternalIdBundle(), identifiers2);
    assertFalse(identifiers1.equals(identifiers2));
  }
  
}