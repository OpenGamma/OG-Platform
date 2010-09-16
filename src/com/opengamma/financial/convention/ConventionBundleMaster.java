/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Repository for rates and associated metadata - e.g. LIBOR/EURIBOR etc...
 */
public interface ConventionBundleMaster {
  ConventionBundleSearchResult searchConventionBundle(ConventionBundleSearchRequest searchRequest);
  ConventionBundleSearchResult searchHistoricConventionBundle(ConventionBundleSearchHistoricRequest searchRequest);
  ConventionBundleDocument getConventionBundle(UniqueIdentifier uniqueIdentifier); 
  UniqueIdentifier addConventionBundle(IdentifierBundle bundle, String name, DayCount dayCount, BusinessDayConvention businessDayConvention, Frequency frequency, int settlementDays);
  UniqueIdentifier addConventionBundle(IdentifierBundle bundle, String name, DayCount dayCount, BusinessDayConvention businessDayConvention, Frequency frequency, 
                                       int settlementDays, double pointValue);
  UniqueIdentifier addConventionBundle(IdentifierBundle bundle, String name,
                                       DayCount swapFixedLegDayCount, BusinessDayConvention swapFixedLegBusinessDayConvention, 
                                       Frequency swapFixedLegFrequency, Integer swapFixedLegSettlementDays,
                                       DayCount swapFloatingLegDayCount, BusinessDayConvention swapFloatingLegBusinessDayConvention, 
                                       Frequency swapFloatingLegFrequency, Integer swapFloatingLegSettlementDays,
                                       Identifier swapFloatingLegInitialRate); 
}
