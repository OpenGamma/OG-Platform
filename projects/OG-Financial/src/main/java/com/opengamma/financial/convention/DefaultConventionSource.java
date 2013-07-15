/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Default implementation of {@link ConventionSource} that uses an underlying {@link ConventionMaster}
 * as a data source.
 */
public class DefaultConventionSource implements ConventionSource {
  /** The convention master */
  private final ConventionMaster _conventionMaster;

  /**
   * @param conventionMaster The convention master, not null
   */
  public DefaultConventionSource(final ConventionMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    _conventionMaster = conventionMaster;
  }

  @Override
  public Convention getConvention(final ExternalId identifier) {
    final ConventionSearchResult result = _conventionMaster.searchConvention(new ConventionSearchRequest(identifier));
    final int size = result.getResults().size();
    switch (size) {
      case 0:
        return null;
      case 1:
        return Iterables.getOnlyElement(result.getResults()).getValue();
      default:
        throw new OpenGammaRuntimeException("Multiple matches (" + size + ") to " + identifier + "; expecting one");
    }
  }

  @Override
  public Convention getConvention(final ExternalIdBundle identifiers) {
    final ConventionSearchResult result = _conventionMaster.searchConvention(new ConventionSearchRequest(identifiers));
    final int size = result.getResults().size();
    switch (size) {
      case 0:
        return null;
      case 1:
        return Iterables.getOnlyElement(result.getResults()).getValue();
      default:
        throw new OpenGammaRuntimeException("Multiple matches (" + size + ") to " + identifiers + "; expecting one");
    }
  }

  @Override
  public Convention getConvention(final UniqueId identifier) {
    final ConventionDocument doc = _conventionMaster.getConvention(identifier);
    if (doc != null) {
      return doc.getValue();
    }
    return null;
  }
}
