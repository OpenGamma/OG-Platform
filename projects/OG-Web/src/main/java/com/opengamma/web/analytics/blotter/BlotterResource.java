/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Set;

import javax.ws.rs.Path;
import javax.xml.ws.Response;

import org.joda.beans.MetaBean;

import com.google.common.collect.Sets;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 *
 */
@Path("blotter")
/* package */ class BlotterResource {

  private static final Set<MetaBean> s_metaBeans = Sets.<MetaBean>newHashSet(
      FXForwardSecurity.meta(),
      SwapSecurity.meta(),
      FixedInterestRateLeg.meta(),
      FloatingInterestRateLeg.meta(),
      FloatingSpreadIRLeg.meta(),
      FloatingGearingIRLeg.meta()
  );

  // TODO endpoint with all known type names

  // TODO HTML page with definition of each type

  // TODO create OTC trade - create new security, position of one, contains one trade

  // TODO create fungible trade - identifier and quantity

  // TODO get OTC trade (security)

  // TODO what about bond futures? is the BondFutureDeliverable part of the definition from bbg?
}
