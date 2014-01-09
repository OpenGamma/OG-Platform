/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import com.opengamma.master.AbstractDataTrackingMaster;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayHistoryRequest;
import com.opengamma.master.holiday.HolidayHistoryResult;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidayMetaDataRequest;
import com.opengamma.master.holiday.HolidayMetaDataResult;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;

/**
 * Holiday master which tracks accesses using UniqueIds.
 */
public class DataTrackingHolidayMaster extends AbstractDataTrackingMaster<HolidayDocument, HolidayMaster> implements HolidayMaster {
  
  public DataTrackingHolidayMaster(HolidayMaster delegate) {
    super(delegate);
  }

  @Override
  public HolidaySearchResult search(HolidaySearchRequest request) {
    HolidaySearchResult searchResult = delegate().search(request);
    trackDocs(searchResult.getDocuments());
    return searchResult;
  }

  @Override
  public HolidayHistoryResult history(HolidayHistoryRequest request) {
    HolidayHistoryResult historyResult = delegate().history(request);
    trackDocs(historyResult.getDocuments());
    return historyResult;
  }

  @Override
  public HolidayMetaDataResult metaData(HolidayMetaDataRequest request) {
    return delegate().metaData(request);
  }
  
  
}
