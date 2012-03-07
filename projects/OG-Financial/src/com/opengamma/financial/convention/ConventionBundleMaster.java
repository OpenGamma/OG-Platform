/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import javax.time.calendar.Period;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/**
 * Repository for rates and associated metadata - e.g. LIBOR/EURIBOR etc...
 */
public interface ConventionBundleMaster {

  ConventionBundleSearchResult searchConventionBundle(ConventionBundleSearchRequest searchRequest);

  ConventionBundleSearchResult searchHistoricConventionBundle(ConventionBundleSearchHistoricRequest searchRequest);

  ConventionBundleDocument getConventionBundle(UniqueId uniqueId);

  UniqueId addConventionBundle(ExternalIdBundle bundle, String name, DayCount dayCount, BusinessDayConvention businessDayConvention, Frequency frequency, int settlementDays);

  UniqueId addConventionBundle(ExternalIdBundle bundle, String name, DayCount dayCount, BusinessDayConvention businessDayConvention, int settlementDays);

  UniqueId addConventionBundle(ExternalIdBundle bundle, String name, DayCount dayCount, BusinessDayConvention businessDayConvention, Period period, int settlementDays, boolean isEOM,
      ExternalId region);

  // Added for Overnight Indices (Case)
  UniqueId addConventionBundle(ExternalIdBundle bundle, String name, DayCount dayCount, BusinessDayConvention businessDayConvention, Period period, int settlementDays, boolean isEOM,
      ExternalId region, Integer publicationLag);

  UniqueId addConventionBundle(ExternalIdBundle bundle, String name, DayCount dayCount, BusinessDayConvention businessDayConvention, Frequency frequency, int settlementDays, double pointValue);

  UniqueId addConventionBundle(ExternalIdBundle bundle, String name, DayCount dayCount, BusinessDayConvention businessDayConvention, int settlementDays, boolean isEOMConvention);

  UniqueId addConventionBundle(ExternalIdBundle bundle, String name, DayCount swapFixedLegDayCount, BusinessDayConvention swapFixedLegBusinessDayConvention, Frequency swapFixedLegFrequency,
      Integer swapFixedLegSettlementDays, ExternalId swapFixedLegRegion, DayCount swapFloatingLegDayCount, BusinessDayConvention swapFloatingLegBusinessDayConvention,
      Frequency swapFloatingLegFrequency, Integer swapFloatingLegSettlementDays, ExternalId swapFloatingLegInitialRate, ExternalId swapFloatingLegRegion);

  UniqueId addConventionBundle(ExternalIdBundle bundle, String name, DayCount swapFixedLegDayCount, BusinessDayConvention swapFixedLegBusinessDayConvention, Frequency swapFixedLegFrequency,
      Integer swapFixedLegSettlementDays, ExternalId swapFixedLegRegion, DayCount swapFloatingLegDayCount, BusinessDayConvention swapFloatingLegBusinessDayConvention,
      Frequency swapFloatingLegFrequency, Integer swapFloatingLegSettlementDays, ExternalId swapFloatingLegInitialRate, ExternalId swapFloatingLegRegion, Boolean isEOM);

  // OIS Overnight Index Swaps
  UniqueId addConventionBundle(ExternalIdBundle bundle, String name, DayCount swapFixedLegDayCount, BusinessDayConvention swapFixedLegBusinessDayConvention, Frequency swapFixedLegFrequency,
      Integer swapFixedLegSettlementDays, ExternalId swapFixedLegRegion, DayCount swapFloatingLegDayCount, BusinessDayConvention swapFloatingLegBusinessDayConvention,
      Frequency swapFloatingLegFrequency, Integer swapFloatingLegSettlementDays, ExternalId swapFloatingLegInitialRate, ExternalId swapFloatingLegRegion, Boolean isEOM, Integer publicationLag);

  UniqueId addConventionBundle(ExternalIdBundle bundle, String name, DayCount basisSwapPayFloatingLegDayCount, BusinessDayConvention basisSwapPayFloatingLegBusinessDayConvention,
      Frequency basisSwapPayFloatingLegFrequency, Integer basisSwapPayFloatingLegSettlementDays, ExternalId basisSwapPayFloatingLegInitialRate, ExternalId basisSwapPayFloatingLegRegion,
      DayCount basisSwapReceiveFloatingLegDayCount, BusinessDayConvention basisSwapReceiveFloatingLegBusinessDayConvention, Frequency basisSwapReceiveFloatingLegFrequency,
      Integer basisSwapReceiveFloatingLegSettlementDays, ExternalId basisSwapReceiveFloatingLegInitialRate, ExternalId basisSwapReceiveFloatingLegRegion);

  UniqueId addConventionBundle(ExternalIdBundle bundle, String name, boolean isEOMConvention, boolean calculateScheduleFromMaturity, int exDividendDays, int settlementDays, boolean rollToSettlement);

  UniqueId addConventionBundle(ExternalIdBundle bundle, String name, boolean isEOMConvention, boolean calculateScheduleFromMaturity, int exDividendDays, int settlementDays, DayCount dayCount,
      BusinessDayConvention businessDayConvention, YieldConvention yieldConvention);

  UniqueId addConventionBundle(ExternalIdBundle bundle, String name, boolean isCashSettled);

  UniqueId addConventionBundle(ExternalIdBundle bundle, String name, ExternalIdBundle capmRiskFreeRate, ExternalIdBundle capmMarket);
}
