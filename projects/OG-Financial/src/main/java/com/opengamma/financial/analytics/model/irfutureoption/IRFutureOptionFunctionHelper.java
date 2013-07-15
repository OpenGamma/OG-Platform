/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Helper class for interest rate future options
 */
public class IRFutureOptionFunctionHelper {

   //TODO REFACTOR LOGIC to permit other schemes and future options */
  /**
   * Finds the IR future option prefix
   * @param target The computation target, not null
   * @return The first two letters of the ticker
   */
  public static String getFutureOptionPrefix(final ComputationTarget target) {
    ArgumentChecker.notNull(target, "target");
    final ExternalIdBundle secId = target.getTrade().getSecurity().getExternalIdBundle();
    String ticker = secId.getValue(ExternalSchemes.BLOOMBERG_TICKER);
    if (ticker == null)  {
      ticker = secId.getValue(ExternalSchemes.ACTIVFEED_TICKER);
    }
    if (ticker != null) {
      final String prefix = ticker.substring(0, 2);
      return prefix;
    } 
    throw new OpenGammaRuntimeException("Could not get ticker for option");
  }

}
