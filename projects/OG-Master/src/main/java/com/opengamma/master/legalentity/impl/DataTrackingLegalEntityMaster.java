/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.legalentity.impl;

import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractDataTrackingMaster;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityHistoryRequest;
import com.opengamma.master.legalentity.LegalEntityHistoryResult;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.LegalEntityMetaDataRequest;
import com.opengamma.master.legalentity.LegalEntityMetaDataResult;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.legalentity.LegalEntitySearchResult;

/**
 * LegalEntity master which tracks accesses using UniqueIds.
 */
public class DataTrackingLegalEntityMaster extends AbstractDataTrackingMaster<LegalEntityDocument, LegalEntityMaster> implements LegalEntityMaster {

  public DataTrackingLegalEntityMaster(LegalEntityMaster delegate) {
    super(delegate);
  }

  @Override
  public LegalEntitySearchResult search(LegalEntitySearchRequest request) {
    LegalEntitySearchResult searchResult = delegate().search(request);
    trackDocs(searchResult.getDocuments());
    return searchResult;
  }

  @Override
  public LegalEntityHistoryResult history(LegalEntityHistoryRequest request) {
    LegalEntityHistoryResult historyResult = delegate().history(request);
    trackDocs(historyResult.getDocuments());
    return historyResult;
  }

  @Override
  public LegalEntityDocument get(UniqueId uid) {
    LegalEntityDocument organization = delegate().get(uid);
    trackId(organization.getUniqueId());
    return organization;
  }

  @Override
  public LegalEntityMetaDataResult metaData(LegalEntityMetaDataRequest request) {
    return delegate().metaData(request);
  }
}
