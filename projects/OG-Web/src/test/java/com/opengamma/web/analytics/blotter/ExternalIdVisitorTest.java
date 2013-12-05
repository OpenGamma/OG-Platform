/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalIdVisitorTest {

  // TODO use test data from BlotterTestUtils
  private static final SwapLeg PAY_LEG =
      new FixedInterestRateLeg(DayCounts.ACT_365,
                               SimpleFrequency.QUARTERLY,
                               ExternalId.of("Reg", "123"),
                               BusinessDayConventions.FOLLOWING,
                               new InterestRateNotional(Currency.GBP, 1234),
                               true,
                               0.01);
  private static final SwapLeg RECEIVE_LEG =
      new FloatingInterestRateLeg(DayCounts.ACT_365,
                                  SimpleFrequency.ANNUAL,
                                  ExternalId.of("Reg", "123"),
                                  BusinessDayConventions.FOLLOWING,
                                  new InterestRateNotional(Currency.GBP, 321),
                                  true,
                                  ExternalId.of("Rate", "1234"),
                                  FloatingRateType.IBOR);

  private static SwapSecurity createSwap() {
    return new SwapSecurity(ZonedDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), "cpty", PAY_LEG, RECEIVE_LEG);
  }

  @Test
  public void swapWithNoExternalId() {
    SecurityMaster securityMaster = mock(SecurityMaster.class);
    ExternalIdVisitor visitor = new ExternalIdVisitor(securityMaster);
    SwapSecurity swap = createSwap();
    UniqueId uid = UniqueId.of("test", "123");
    swap.setUniqueId(uid);
    assertTrue(swap.getExternalIdBundle().isEmpty());
    swap.accept(visitor);
    assertEquals(ExternalId.of(ObjectId.EXTERNAL_SCHEME, uid.getObjectId().toString()),
                 swap.getExternalIdBundle().getExternalId(ObjectId.EXTERNAL_SCHEME));
    verify(securityMaster).update(new SecurityDocument(swap));
  }

  @Test
  public void swapWithExternalId() {
    SecurityMaster securityMaster = mock(SecurityMaster.class);
    ExternalIdVisitor visitor = new ExternalIdVisitor(securityMaster);
    SwapSecurity swap = createSwap();
    UniqueId uid = UniqueId.of("test", "123");
    swap.setUniqueId(uid);
    ExternalId externalId = ExternalId.of(UniqueId.EXTERNAL_SCHEME, "345");
    swap.setExternalIdBundle(ExternalIdBundle.of(externalId));
    swap.accept(visitor);
    assertEquals(externalId, swap.getExternalIdBundle().getExternalId(UniqueId.EXTERNAL_SCHEME));
    verify(securityMaster, never()).update(any(SecurityDocument.class));
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void swapWithNoUniqueId() {
    SecurityMaster securityMaster = mock(SecurityMaster.class);
    ExternalIdVisitor visitor = new ExternalIdVisitor(securityMaster);
    SwapSecurity swap = createSwap();
    swap.accept(visitor);
  }

  @Test
  public void otherSecurityType() {
    SecurityMaster securityMaster = mock(SecurityMaster.class);
    ExternalIdVisitor visitor = new ExternalIdVisitor(securityMaster);
    FXForwardSecurity security = new FXForwardSecurity(Currency.GBP, 123, Currency.AUD, 321, ZonedDateTime.now(),
                                                       ExternalId.of("reg", "123"));
    security.accept(visitor);
    assertTrue(security.getExternalIdBundle().isEmpty());
  }
}
