/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleExchangeNameSecurityId;

import com.opengamma.financial.convention.expirycalc.BondFutureOptionExpiryCalculator;
import com.opengamma.financial.convention.expirycalc.IMMFutureAndFutureOptionQuarterlyExpiryCalculator;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ExchangeConventions {

  public static synchronized void addExchangeFutureOptionConventions(final InMemoryConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    utils.addConventionBundle(ExternalIdBundle.of(simpleExchangeNameSecurityId("CME")), IRFutureOptionSecurity.SECURITY_TYPE, IMMFutureAndFutureOptionQuarterlyExpiryCalculator.NAME);
    utils.addConventionBundle(ExternalIdBundle.of(simpleExchangeNameSecurityId("CBT")), BondFutureOptionSecurity.SECURITY_TYPE, BondFutureOptionExpiryCalculator.NAME);
    utils.addConventionBundle(ExternalIdBundle.of(simpleExchangeNameSecurityId("EUX")), BondFutureOptionSecurity.SECURITY_TYPE, BondFutureOptionExpiryCalculator.NAME);
  }
}
