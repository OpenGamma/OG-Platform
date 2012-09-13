/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.impl;

import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataError;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;

/**
 * Simple implementation of a provider of reference-data that finds nothing.
 */
public class NoneFoundReferenceDataProvider extends AbstractReferenceDataProvider {

  /**
   * Creates an instance.
   */
  public NoneFoundReferenceDataProvider() {
  }

  //-------------------------------------------------------------------------
  @Override
  protected ReferenceDataProviderGetResult doBulkGet(ReferenceDataProviderGetRequest request) {
    ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    for (String identifier : request.getIdentifiers()) {
      ReferenceData rd = new ReferenceData(identifier);
      rd.addError(new ReferenceDataError(null, -1, null, null, "None found"));
      result.addReferenceData(rd);
    }
    return result;
  }

}
