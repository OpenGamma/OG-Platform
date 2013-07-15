/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import org.threeten.bp.Period;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.time.Tenor;

/**
 * Class of utility methods for adding convention bundles to a convention bundle master
 */
public class ConventionBundleMasterUtils {
  private final ConventionBundleMaster _master;

  public ConventionBundleMasterUtils(final ConventionBundleMaster master) {
    _master = master;
  }

  private UniqueId add(final ExternalIdBundle bundle, final ConventionBundleImpl conventionBundle) {
    return _master.add(bundle, conventionBundle);
  }

  //-------------------------------------------------------------------------
  public synchronized UniqueId addConventionBundle(final ExternalIdBundle bundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention,
      final Frequency frequency, final int settlementDays) {
    final ConventionBundleImpl convention = new ConventionBundleImpl(bundle, name, dayCount, businessDayConvention, frequency, settlementDays);
    return add(bundle, convention);
  }

  public synchronized UniqueId addConventionBundle(final ExternalIdBundle bundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention,
      final Frequency frequency, final int settlementDays, final double yearFraction) {
    final ConventionBundleImpl convention = new ConventionBundleImpl(bundle, name, dayCount, businessDayConvention, frequency, settlementDays, yearFraction);
    return add(bundle, convention);
  }

  public synchronized UniqueId addConventionBundle(final ExternalIdBundle bundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention,
      final int settlementDays) {
    final ConventionBundleImpl convention = new ConventionBundleImpl(bundle, name, dayCount, businessDayConvention, settlementDays);
    return add(bundle, convention);
  }

  public synchronized UniqueId addConventionBundle(final ExternalIdBundle bundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final Period period,
      final int settlementDays, final boolean isEOM, final ExternalId region) {
    final ConventionBundleImpl convention = new ConventionBundleImpl(bundle, name, dayCount, businessDayConvention, period, settlementDays, isEOM, region);
    return add(bundle, convention);
  }

  // (Case) Overnight Indices
  public synchronized UniqueId addConventionBundle(final ExternalIdBundle bundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final Period period,
      final int settlementDays, final boolean isEOM, final ExternalId region, final Integer publicationLag) {
    final ConventionBundleImpl convention = new ConventionBundleImpl(bundle, name, dayCount, businessDayConvention, period, settlementDays, isEOM, region, publicationLag);
    return add(bundle, convention);
  }

  public synchronized UniqueId addConventionBundle(final ExternalIdBundle bundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention,
      final int settlementDays, final boolean isEOMConvention) {
    final ConventionBundleImpl convention = new ConventionBundleImpl(bundle, name, dayCount, businessDayConvention, settlementDays);
    return add(bundle, convention);
  }

  public synchronized UniqueId addConventionBundle(final ExternalIdBundle bundle, final String name, final DayCount swapFixedLegDayCount,
      final BusinessDayConvention swapFixedLegBusinessDayConvention, final Frequency swapFixedLegFrequency, final Integer swapFixedLegSettlementDays, final ExternalId swapFixedLegRegion,
      final DayCount swapFloatingLegDayCount, final BusinessDayConvention swapFloatingLegBusinessDayConvention, final Frequency swapFloatingLegFrequency, final Integer swapFloatingLegSettlementDays,
      final ExternalId swapFloatingLegInitialRate, final ExternalId swapFloatingLegRegion) {
    final ConventionBundleImpl convention = new ConventionBundleImpl(bundle, name, swapFixedLegDayCount, swapFixedLegBusinessDayConvention, swapFixedLegFrequency, swapFixedLegSettlementDays,
        swapFixedLegRegion, swapFloatingLegDayCount, swapFloatingLegBusinessDayConvention, swapFloatingLegFrequency, swapFloatingLegSettlementDays, swapFloatingLegInitialRate, swapFloatingLegRegion);
    return add(bundle, convention);
  }

  // IRswap and FRA
  public synchronized UniqueId addConventionBundle(final ExternalIdBundle bundle, final String name, final DayCount swapFixedLegDayCount,
      final BusinessDayConvention swapFixedLegBusinessDayConvention, final Frequency swapFixedLegFrequency, final Integer swapFixedLegSettlementDays, final ExternalId swapFixedLegRegion,
      final DayCount swapFloatingLegDayCount, final BusinessDayConvention swapFloatingLegBusinessDayConvention, final Frequency swapFloatingLegFrequency, final Integer swapFloatingLegSettlementDays,
      final ExternalId swapFloatingLegInitialRate, final ExternalId swapFloatingLegRegion, final Boolean isEOM) {
    final ConventionBundleImpl convention = new ConventionBundleImpl(bundle, name, swapFixedLegDayCount, swapFixedLegBusinessDayConvention, swapFixedLegFrequency, swapFixedLegSettlementDays,
        swapFixedLegRegion, swapFloatingLegDayCount, swapFloatingLegBusinessDayConvention, swapFloatingLegFrequency, swapFloatingLegSettlementDays, swapFloatingLegInitialRate, swapFloatingLegRegion,
        isEOM);
    return add(bundle, convention);
  }

  // IRswap indexes
  public synchronized UniqueId addConventionBundle(final ExternalIdBundle bundle, final String name, final DayCount swapFixedLegDayCount,
      final BusinessDayConvention swapFixedLegBusinessDayConvention, final Frequency swapFixedLegFrequency, final Integer swapFixedLegSettlementDays, final ExternalId swapFixedLegRegion,
      final DayCount swapFloatingLegDayCount, final BusinessDayConvention swapFloatingLegBusinessDayConvention, final Frequency swapFloatingLegFrequency, final Integer swapFloatingLegSettlementDays,
      final ExternalId swapFloatingLegInitialRate, final ExternalId swapFloatingLegRegion, final Boolean isEOM, final Period swapTenor) {
    final ConventionBundleImpl convention = new ConventionBundleImpl(bundle, name, swapFixedLegDayCount, swapFixedLegBusinessDayConvention, swapFixedLegFrequency, swapFixedLegSettlementDays,
        swapFixedLegRegion, swapFloatingLegDayCount, swapFloatingLegBusinessDayConvention, swapFloatingLegFrequency, swapFloatingLegSettlementDays, swapFloatingLegInitialRate, swapFloatingLegRegion,
        isEOM, swapTenor);
    return add(bundle, convention);
  }

  public synchronized UniqueId addConventionBundle(final ExternalIdBundle bundle, final String name, final DayCount swapFixedLegDayCount,
      final BusinessDayConvention swapFixedLegBusinessDayConvention, final Frequency swapFixedLegFrequency, final Integer swapFixedLegSettlementDays, final ExternalId swapFixedLegRegion,
      final DayCount swapFloatingLegDayCount, final BusinessDayConvention swapFloatingLegBusinessDayConvention, final Frequency swapFloatingLegFrequency, final Integer swapFloatingLegSettlementDays,
      final ExternalId swapFloatingLegInitialRate, final ExternalId swapFloatingLegRegion, final Boolean isEOM, final Integer publicationLag) {
    final ConventionBundleImpl convention = new ConventionBundleImpl(bundle, name, swapFixedLegDayCount, swapFixedLegBusinessDayConvention, swapFixedLegFrequency, swapFixedLegSettlementDays,
        swapFixedLegRegion, swapFloatingLegDayCount, swapFloatingLegBusinessDayConvention, swapFloatingLegFrequency, swapFloatingLegSettlementDays, swapFloatingLegInitialRate, swapFloatingLegRegion,
        isEOM, publicationLag);
    return add(bundle, convention);
  }

  // Basis swap
  public UniqueId addConventionBundle(final ExternalIdBundle bundle, final String name, final DayCount basisSwapPayFloatingLegDayCount,
      final BusinessDayConvention basisSwapPayFloatingLegBusinessDayConvention, final Frequency basisSwapPayFloatingLegFrequency, final Integer basisSwapPayFloatingLegSettlementDays,
      final ExternalId basisSwapPayFloatingLegInitialRate, final ExternalId basisSwapPayFloatingLegRegion, final DayCount basisSwapReceiveFloatingLegDayCount,
      final BusinessDayConvention basisSwapReceiveFloatingLegBusinessDayConvention, final Frequency basisSwapReceiveFloatingLegFrequency, final Integer basisSwapReceiveFloatingLegSettlementDays,
      final ExternalId basisSwapReceiveFloatingLegInitialRate, final ExternalId basisSwapReceiveFloatingLegRegion) {
    final ConventionBundleImpl convention = new ConventionBundleImpl(bundle, name, basisSwapPayFloatingLegDayCount, basisSwapPayFloatingLegBusinessDayConvention, basisSwapPayFloatingLegFrequency,
        basisSwapPayFloatingLegSettlementDays, basisSwapPayFloatingLegInitialRate, basisSwapPayFloatingLegRegion, basisSwapReceiveFloatingLegDayCount,
        basisSwapReceiveFloatingLegBusinessDayConvention, basisSwapReceiveFloatingLegFrequency, basisSwapReceiveFloatingLegSettlementDays, basisSwapReceiveFloatingLegInitialRate,
        basisSwapReceiveFloatingLegRegion);
    return add(bundle, convention);
  }

  public synchronized UniqueId addConventionBundle(final ExternalIdBundle bundle, final String name, final ExternalIdBundle capmRiskFreeRate, final ExternalIdBundle capmMarket) {
    final ConventionBundleImpl convention = new ConventionBundleImpl(name, capmRiskFreeRate, capmMarket);
    return add(bundle, convention);
  }

  public synchronized UniqueId addConventionBundle(final ExternalIdBundle bundle, final String name, final boolean isEOMConvention, final boolean calculateScheduleFromMaturity,
      final int exDividendDays, final int settlementDays, final boolean rollToSettlement) {
    final ConventionBundleImpl convention = new ConventionBundleImpl(name, isEOMConvention, calculateScheduleFromMaturity, exDividendDays, settlementDays, rollToSettlement);
    return add(bundle, convention);
  }

  public synchronized UniqueId addConventionBundle(final ExternalIdBundle bundle, final String name, final boolean isEOMConvention, final boolean calculateScheduleFromMaturity,
      final int exDividendDays, final int shortSettlementDays, final int longSettlementDays, final boolean rollToSettlement, final Tenor cutoffTenor) {
    final ConventionBundleImpl convention = new ConventionBundleImpl(name, isEOMConvention, calculateScheduleFromMaturity, exDividendDays, shortSettlementDays, longSettlementDays, rollToSettlement,
        cutoffTenor);
    return add(bundle, convention);
  }

  public synchronized UniqueId addConventionBundle(final ExternalIdBundle bundle, final String name, final boolean isEOMConvention, final boolean calculateScheduleFromMaturity,
      final int exDividendDays, final int settlementDays, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final YieldConvention yieldConvention) {
    final ConventionBundleImpl convention = new ConventionBundleImpl(name, isEOMConvention, calculateScheduleFromMaturity, exDividendDays, settlementDays, dayCount, businessDayConvention,
        yieldConvention);
    return add(bundle, convention);
  }

  public synchronized UniqueId addConventionBundle(final ExternalIdBundle bundle, final String name, final boolean isCashSettled) {
    final ConventionBundleImpl convention = new ConventionBundleImpl(name, isCashSettled);
    return add(bundle, convention);
  }

  public synchronized UniqueId addConventionBundle(final ExternalIdBundle bundle, final String name, final DayCount dayCount) {
    final ConventionBundleImpl convention = new ConventionBundleImpl(bundle, name, dayCount, null, null, 0);
    return add(bundle, convention);
  }

  public synchronized UniqueId addConventionBundle(final ExternalIdBundle bundle, final String name, final String expiryCalculatorName) {
    final ConventionBundleImpl convention = new ConventionBundleImpl(bundle, name, expiryCalculatorName);
    return add(bundle, convention);
  }
}
