/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.core.convention.BusinessDayConvention;
import com.opengamma.core.convention.DayCount;
import com.opengamma.core.convention.Frequency;
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

  UniqueIdentifier addConventionBundle(IdentifierBundle bundle, String name, DayCount swapFixedLegDayCount, BusinessDayConvention swapFixedLegBusinessDayConvention,
      Frequency swapFixedLegFrequency, Integer swapFixedLegSettlementDays, Identifier swapFixedLegRegion,
      DayCount swapFloatingLegDayCount, BusinessDayConvention swapFloatingLegBusinessDayConvention,
      Frequency swapFloatingLegFrequency, Integer swapFloatingLegSettlementDays, Identifier swapFloatingLegInitialRate, Identifier swapFloatingLegRegion);

  UniqueIdentifier addConventionBundle(IdentifierBundle bundle, String name,
      DayCount basisSwapPayFloatingLegDayCount, BusinessDayConvention basisSwapPayFloatingLegBusinessDayConvention, Frequency basisSwapPayFloatingLegFrequency,
      Integer basisSwapPayFloatingLegSettlementDays, Identifier basisSwapPayFloatingLegInitialRate, Identifier basisSwapPayFloatingLegRegion,
      DayCount basisSwapReceiveFloatingLegDayCount, BusinessDayConvention basisSwapReceiveFloatingLegBusinessDayConvention, Frequency basisSwapReceiveFloatingLegFrequency,
      Integer basisSwapReceiveFloatingLegSettlementDays, Identifier basisSwapReceiveFloatingLegInitialRate, Identifier basisSwapReceiveFloatingLegRegion);

  UniqueIdentifier addConventionBundle(IdentifierBundle bundle, String name, String capmRiskFreeRateName, String capmMarketName);

  UniqueIdentifier addConventionBundle(IdentifierBundle bundle, String name, boolean isEOMConvention, boolean calculateScheduleFromMaturity, int exDividendDays, int settlementDays);
}
