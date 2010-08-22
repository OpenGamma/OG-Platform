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
public class DefaultReferenceRateSource implements ReferenceRateSource {

  private ReferenceRateRepository _referenceRateMaster;
  
  public DefaultReferenceRateSource(ReferenceRateRepository referenceRateMaster) {
    _referenceRateMaster = referenceRateMaster;
  }
  @Override
  public ReferenceRate getSingleReferenceRate(Identifier identifier) {
    ReferenceRateSearchResult result = _referenceRateMaster.searchReferenceRates(new ReferenceRateSearchRequest(identifier));
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
  public ReferenceRate getSingleReferenceRate(IdentifierBundle identifiers) {
    ReferenceRateSearchResult result = _referenceRateMaster.searchReferenceRates(new ReferenceRateSearchRequest(identifiers));
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
  public ReferenceRate getReferenceRate(UniqueIdentifier identifier) {
    ReferenceRateDocument doc = _referenceRateMaster.getReferenceRate(identifier);
    if (doc != null) {
      return doc.getValue();
    } else {
      return null;
    }
  }

}
