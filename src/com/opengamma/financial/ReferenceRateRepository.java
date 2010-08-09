/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Repository for rates and associated metadata - e.g. LIBOR/EURIBOR etc...
 */
public interface ReferenceRateRepository {
  ReferenceRateSearchResult searchReferenceRates(ReferenceRateSearchRequest searchRequest);
  ReferenceRateSearchResult searchHistoricReferenceRates(ReferenceRateSearchHistoricRequest searchRequest);
  ReferenceRateDocument getReferenceRate(UniqueIdentifier uniqueIdentifier);
  UniqueIdentifier addReferenceRate(IdentifierBundle bundle, String name, DayCount dayCount, BusinessDayConvention businessDayConvention, int settlementDays);
}
