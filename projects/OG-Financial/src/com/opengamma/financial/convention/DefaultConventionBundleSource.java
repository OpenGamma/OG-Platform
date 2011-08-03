/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueId;

/**
 * Default implementation of ReferenceRateSource that uses an underlying ReferenceRateMaster as a data source. 
 */
public class DefaultConventionBundleSource implements ConventionBundleSource {

  private ConventionBundleMaster _referenceRateMaster;
  
  public DefaultConventionBundleSource(ConventionBundleMaster referenceRateMaster) {
    _referenceRateMaster = referenceRateMaster;
  }
  @Override
  public ConventionBundle getConventionBundle(Identifier identifier) {
    ConventionBundleSearchResult result = _referenceRateMaster.searchConventionBundle(new ConventionBundleSearchRequest(identifier));
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
  public ConventionBundle getConventionBundle(IdentifierBundle identifiers) {
    ConventionBundleSearchResult result = _referenceRateMaster.searchConventionBundle(new ConventionBundleSearchRequest(identifiers));
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
  public ConventionBundle getConventionBundle(UniqueId identifier) {
    ConventionBundleDocument doc = _referenceRateMaster.getConventionBundle(identifier);
    if (doc != null) {
      return doc.getValue();
    } else {
      return null;
    }
  }

}
