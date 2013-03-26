/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.resolver.AbstractSourceResolver;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.VersionCorrection;

/**
 * An engine resolver for {@link CurrencyMatrix} objects
 */
public class CurrencyMatrixResolver extends AbstractSourceResolver<CurrencyMatrix, CurrencyMatrixSource> {

  /**
   * The type used to indicate the currency matrix.
   */
  public static final ComputationTargetType TYPE = ComputationTargetType.of(CurrencyMatrix.class);

  /**
   * The identifier scheme for external identifiers that reference a matrix by name.
   */
  public static final ExternalScheme IDENTIFIER_SCHEME = ExternalScheme.of(CurrencyMatrix.class.getSimpleName());

  public CurrencyMatrixResolver(final CurrencyMatrixSource underlying) {
    super(IDENTIFIER_SCHEME, underlying);
  }

  // AbstractSourceResolver

  @Override
  protected CurrencyMatrix lookupByName(final String name, final VersionCorrection versionCorrection) {
    return getUnderlying().getCurrencyMatrix(name, versionCorrection);
  }

}
