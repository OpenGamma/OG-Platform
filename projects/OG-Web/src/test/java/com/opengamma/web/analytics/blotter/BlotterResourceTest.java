/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.master.security.ManageableSecurity;

public class BlotterResourceTest {

  @Test
  public void isSecurity() {
    assertTrue(BlotterResource.isSecurity(ManageableSecurity.class));
    assertTrue(BlotterResource.isSecurity(SwapSecurity.class));
    assertTrue(BlotterResource.isSecurity(GovernmentBondSecurity.class));
    assertFalse(BlotterResource.isSecurity(FixedInterestRateLeg.class));
  }
}
