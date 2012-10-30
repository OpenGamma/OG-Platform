/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondfutureoption;

import com.google.common.collect.Iterables;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 *
 */
public class BondFutureOptionUtils {

  public static ExternalId getBloombergTicker(final BondFutureOptionSecurity security) {
    final ExternalIdBundle bundle = security.getExternalIdBundle();
    return Iterables.getOnlyElement(bundle.getExternalIds(ExternalSchemes.BLOOMBERG_TICKER));
  }

  public static ExternalId getCallBloombergTicker(final BondFutureOptionSecurity security) {
    final ExternalId id = getBloombergTicker(security);
    if (security.getOptionType().equals(OptionType.CALL)) {
      return id;
    }
    final String ticker = id.getValue();
    final String putTicker = ticker.replaceFirst("P", "C");
    return ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, putTicker);
  }

  public static ExternalId getPutBloombergTicker(final BondFutureOptionSecurity security) {
    final ExternalId id = getBloombergTicker(security);
    if (security.getOptionType().equals(OptionType.PUT)) {
      return id;
    }
    final String ticker = id.getValue();
    final String putTicker = ticker.replaceFirst("C", "P");
    return ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, putTicker);
  }
}
