/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Default implementation of ReferenceRateSource that uses an underlying ReferenceRateMaster as a data source. 
 */
public class DefaultReferenceRateSource implements ConventionBundleSource {

  private ConventionBundleMaster _referenceRateMaster;
  
  public DefaultReferenceRateSource(ConventionBundleMaster referenceRateMaster) {
    _referenceRateMaster = referenceRateMaster;
  }
  @Override
  public ConventionBundle getSingleReferenceRate(Identifier identifier) {
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
  public ConventionBundle getSingleReferenceRate(IdentifierBundle identifiers) {
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
  public ConventionBundle getReferenceRate(UniqueIdentifier identifier) {
    ConventionBundleDocument doc = _referenceRateMaster.getConventionBundle(identifier);
    if (doc != null) {
      return doc.getValue();
    } else {
      return null;
    }
  }

}
