/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/**
 * Default implementation of ConventinBundleSource that uses an underlying ConventionBundleMaster as a data source.
 */
public class DefaultConventionBundleSource implements ConventionBundleSource {

  private final ConventionBundleMaster _referenceRateMaster;

  public DefaultConventionBundleSource(final ConventionBundleMaster referenceRateMaster) {
    _referenceRateMaster = referenceRateMaster;
  }
  @Override
  public ConventionBundle getConventionBundle(final ExternalId identifier) {
    final ConventionBundleSearchResult result = _referenceRateMaster.searchConventionBundle(new ConventionBundleSearchRequest(identifier));
    final int size = result.getResults().size();
    switch (size) {
      case 0:
        return null;
      case 1:
        return result.getResults().iterator().next().getValue();
      default:
        throw new OpenGammaRuntimeException("Multiple matches (" + size + ") to " + identifier + ", expecting one");
    }
  }

  @Override
  public ConventionBundle getConventionBundle(final ExternalIdBundle identifiers) {
    final ConventionBundleSearchResult result = _referenceRateMaster.searchConventionBundle(new ConventionBundleSearchRequest(identifiers));
    final int size = result.getResults().size();
    switch (size) {
      case 0:
        return null;
      case 1:
        return result.getResults().iterator().next().getValue();
      default:
        throw new OpenGammaRuntimeException("Multiple matches (" + size + ") to " + identifiers + ", expecting one");
    }
  }

  @Override
  public ConventionBundle getConventionBundle(final UniqueId identifier) {
    final ConventionBundleDocument doc = _referenceRateMaster.getConventionBundle(identifier);
    if (doc != null) {
      return doc.getValue();
    } else {
      return null;
    }
  }

}
