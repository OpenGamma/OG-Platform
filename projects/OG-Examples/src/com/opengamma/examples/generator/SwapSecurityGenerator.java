/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.generator.AbstractSwapSecurityGenerator;
import com.opengamma.id.ExternalId;
import com.opengamma.util.tuple.Pair;

/**
 * Source of random, but reasonable, swap security instances.
 */
public class SwapSecurityGenerator extends AbstractSwapSecurityGenerator {

  @Override
  protected String getCurveConfigName() {
    return "SECONDARY";
  }

  @Override
  protected Pair<ExternalId, String> getTimeSeriesConvention(final ConventionBundle liborConvention) {
    return Pair.of(liborConvention.getIdentifiers().getExternalId(SecurityUtils.OG_SYNTHETIC_TICKER), MarketDataRequirementNames.MARKET_VALUE);
  }
}
