/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.engine.target.resolver.AbstractSourceResolver;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.VersionCorrection;

/**
 * An engine resolver for {@link CurrencyPairs} objects
 */
public class CurrencyPairsResolver extends AbstractSourceResolver<CurrencyPairs, VersionedCurrencyPairsSource> {

  /**
   * The scheme used to reference convention documents by symbolic name.
   */
  public static final ExternalScheme IDENTIFIER_SCHEME = ExternalScheme.of("CurrencyPairs");

  public CurrencyPairsResolver(final VersionedCurrencyPairsSource underlying) {
    super(IDENTIFIER_SCHEME, underlying);
  }

  // AbstractSourceResolver

  @Override
  protected CurrencyPairs lookupByName(final String name, final VersionCorrection versionCorrection) {
    return getUnderlying().getCurrencyPairs(name, versionCorrection);
  }

}
