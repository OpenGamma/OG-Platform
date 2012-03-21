/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/**
 * An implementation of ReferenceRate for use by the InMemoryReferenceRateRepository.  Note it is NOT immutable, because the repository needs to be able to assign 
 * unique ids to it retrospectively, and to be able to update the bundle.
 */
public class ConventionBundleImpl implements ConventionBundle {

  private UniqueId _uniqueId;
  private ExternalIdBundle _bundle;
  private final String _name;
  private DayCount _dayCount;
  private BusinessDayConvention _businessDayConvention;
  private Integer _settlementDays;
  private Frequency _frequency;
  private Period _period;
  private ExternalId _region;

  private DayCount _swapFixedLegDayCount;
  private BusinessDayConvention _swapFixedLegBusinessDayConvention;
  private Frequency _swapFixedLegFrequency;
  private Integer _swapFixedLegSettlementDays;
  private DayCount _swapFloatingLegDayCount;
  private BusinessDayConvention _swapFloatingLegBusinessDayConvention;
  private Frequency _swapFloatingLegFrequency;
  private Integer _swapFloatingLegSettlementDays;
  private ExternalId _swapFloatingLegInitialRate;
  private Double _yearFraction;

  private Integer _publicationLag;

  //Equity models
  private ExternalIdBundle _capmRiskFreeRate;
  private ExternalIdBundle _capmMarket;

  // basis swaps
  private DayCount _basisSwapPayFloatingLegDayCount;
  private BusinessDayConvention _basisSwapPayFloatingLegBusinessDayConvention;
  private Frequency _basisSwapPayFloatingLegFrequency;
  private Integer _basisSwapPayFloatingLegSettlementDays;
  private ExternalId _basisSwapPayFloatingLegInitialRate;
  private ExternalId _basisSwapPayFloatingLegRegion;
  private DayCount _basisSwapReceiveFloatingLegDayCount;
  private BusinessDayConvention _basisSwapReceiveFloatingLegBusinessDayConvention;
  private Frequency _basisSwapReceiveFloatingLegFrequency;
  private Integer _basisSwapReceiveFloatingLegSettlementDays;
  private ExternalId _basisSwapReceiveFloatingLegInitialRate;
  private ExternalId _basisSwapReceiveFloatingLegRegion;
  private ExternalId _swapFixedLegRegion;
  private ExternalId _swapFloatingLegRegion;

  // bonds
  private Boolean _isEOMConvention;
  private Boolean _calculateScheduleFromMaturity;
  private int _exDividendDays;
  private YieldConvention _yieldConvention;
  private boolean _rollToSettlement;

  //swaptions
  private boolean _isCashSettled;

  // cash/general
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final Frequency frequency,
      final int settlementDays) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _frequency = frequency;
    _settlementDays = settlementDays;
  }

  // generic cash
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final int settlementDays) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _settlementDays = settlementDays;
  }

  // cash/general
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final Period period,
      final int settlementDays, final ExternalId region) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _period = period;
    _settlementDays = settlementDays;
    _region = region;
  }

  // cash/general - with EOM indicated
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final Period period,
      final int settlementDays, final boolean isEOM, final ExternalId region) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _period = period;
    _settlementDays = settlementDays;
    _isEOMConvention = isEOM;
    _region = region;
  }

  // ibor indices that act as underlyings (e.g. floating reference rate for swaps)
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final int settlementDays,
      final boolean isEOMConvention) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _settlementDays = settlementDays;
    _isEOMConvention = isEOMConvention;
  }

  // Overnight Indices
  public ConventionBundleImpl(final ExternalIdBundle bundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final Period period,
      final int settlementDays, final boolean isEOM, final ExternalId region, final Integer publicationLag) {
    _bundle = bundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _period = period;
    _settlementDays = settlementDays;
    _isEOMConvention = isEOM;
    _region = region;
    _publicationLag = publicationLag;
  }

  // futures
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final Frequency frequency,
      final int settlementDays, final double yearFraction) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _frequency = frequency;
    _settlementDays = settlementDays;
    _yearFraction = yearFraction;
  }

  // Swaps and FRA without EOM
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount swapFixedLegDayCount, final BusinessDayConvention swapFixedLegBusinessDayConvention,
      final Frequency swapFixedLegFrequency, final Integer swapFixedLegSettlementDays, final ExternalId swapFixedLegRegion, final DayCount swapFloatingLegDayCount,
      final BusinessDayConvention swapFloatingLegBusinessDayConvention, final Frequency swapFloatingLegFrequency, final Integer swapFloatingLegSettlementDays,
      final ExternalId swapFloatingLegInitialRate, final ExternalId swapFloatingLegRegion) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = null;
    _businessDayConvention = null;
    _frequency = null;
    _settlementDays = null;
    _swapFixedLegDayCount = swapFixedLegDayCount;
    _swapFixedLegBusinessDayConvention = swapFixedLegBusinessDayConvention;
    _swapFixedLegFrequency = swapFixedLegFrequency;
    _swapFixedLegSettlementDays = swapFixedLegSettlementDays;
    _swapFixedLegRegion = swapFixedLegRegion;
    _swapFloatingLegDayCount = swapFloatingLegDayCount;
    _swapFloatingLegBusinessDayConvention = swapFloatingLegBusinessDayConvention;
    _swapFloatingLegFrequency = swapFloatingLegFrequency;
    _swapFloatingLegSettlementDays = swapFloatingLegSettlementDays;
    _swapFloatingLegInitialRate = swapFloatingLegInitialRate;
    _swapFloatingLegRegion = swapFloatingLegRegion;
  }

  // Swaps and FRA
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount swapFixedLegDayCount, final BusinessDayConvention swapFixedLegBusinessDayConvention,
      final Frequency swapFixedLegFrequency, final Integer swapFixedLegSettlementDays, final ExternalId swapFixedLegRegion, final DayCount swapFloatingLegDayCount,
      final BusinessDayConvention swapFloatingLegBusinessDayConvention, final Frequency swapFloatingLegFrequency, final Integer swapFloatingLegSettlementDays,
      final ExternalId swapFloatingLegInitialRate, final ExternalId swapFloatingLegRegion, final boolean isEOM) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = null;
    _businessDayConvention = null;
    _frequency = null;
    _settlementDays = null;
    _swapFixedLegDayCount = swapFixedLegDayCount;
    _swapFixedLegBusinessDayConvention = swapFixedLegBusinessDayConvention;
    _swapFixedLegFrequency = swapFixedLegFrequency;
    _swapFixedLegSettlementDays = swapFixedLegSettlementDays;
    _swapFixedLegRegion = swapFixedLegRegion;
    _swapFloatingLegDayCount = swapFloatingLegDayCount;
    _swapFloatingLegBusinessDayConvention = swapFloatingLegBusinessDayConvention;
    _swapFloatingLegFrequency = swapFloatingLegFrequency;
    _swapFloatingLegSettlementDays = swapFloatingLegSettlementDays;
    _swapFloatingLegInitialRate = swapFloatingLegInitialRate;
    _swapFloatingLegRegion = swapFloatingLegRegion;
    _isEOMConvention = isEOM;
  }

  // Swaps indexes
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount swapFixedLegDayCount, final BusinessDayConvention swapFixedLegBusinessDayConvention,
      final Frequency swapFixedLegFrequency, final Integer swapFixedLegSettlementDays, final ExternalId swapFixedLegRegion, final DayCount swapFloatingLegDayCount,
      final BusinessDayConvention swapFloatingLegBusinessDayConvention, final Frequency swapFloatingLegFrequency, final Integer swapFloatingLegSettlementDays,
      final ExternalId swapFloatingLegInitialRate, final ExternalId swapFloatingLegRegion, final boolean isEOM, final Period swapTenor) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = null;
    _businessDayConvention = null;
    _frequency = null;
    _settlementDays = null;
    _swapFixedLegDayCount = swapFixedLegDayCount;
    _swapFixedLegBusinessDayConvention = swapFixedLegBusinessDayConvention;
    _swapFixedLegFrequency = swapFixedLegFrequency;
    _swapFixedLegSettlementDays = swapFixedLegSettlementDays;
    _swapFixedLegRegion = swapFixedLegRegion;
    _swapFloatingLegDayCount = swapFloatingLegDayCount;
    _swapFloatingLegBusinessDayConvention = swapFloatingLegBusinessDayConvention;
    _swapFloatingLegFrequency = swapFloatingLegFrequency;
    _swapFloatingLegSettlementDays = swapFloatingLegSettlementDays;
    _swapFloatingLegInitialRate = swapFloatingLegInitialRate;
    _swapFloatingLegRegion = swapFloatingLegRegion;
    _isEOMConvention = isEOM;
    _period = swapTenor;
  }

  // OIS Swaps
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount swapFixedLegDayCount, final BusinessDayConvention swapFixedLegBusinessDayConvention,
      final Frequency swapFixedLegFrequency, final Integer swapFixedLegSettlementDays, final ExternalId swapFixedLegRegion, final DayCount swapFloatingLegDayCount,
      final BusinessDayConvention swapFloatingLegBusinessDayConvention, final Frequency swapFloatingLegFrequency, final Integer swapFloatingLegSettlementDays,
      final ExternalId swapFloatingLegInitialRate, final ExternalId swapFloatingLegRegion, final boolean isEOM, final Integer publicationLag) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = null;
    _businessDayConvention = null;
    _frequency = null;
    _settlementDays = null;
    _swapFixedLegDayCount = swapFixedLegDayCount;
    _swapFixedLegBusinessDayConvention = swapFixedLegBusinessDayConvention;
    _swapFixedLegFrequency = swapFixedLegFrequency;
    _swapFixedLegSettlementDays = swapFixedLegSettlementDays;
    _swapFixedLegRegion = swapFixedLegRegion;
    _swapFloatingLegDayCount = swapFloatingLegDayCount;
    _swapFloatingLegBusinessDayConvention = swapFloatingLegBusinessDayConvention;
    _swapFloatingLegFrequency = swapFloatingLegFrequency;
    _swapFloatingLegSettlementDays = swapFloatingLegSettlementDays;
    _swapFloatingLegInitialRate = swapFloatingLegInitialRate;
    _swapFloatingLegRegion = swapFloatingLegRegion;
    _isEOMConvention = isEOM;
    _publicationLag = publicationLag;
  }

  // basis swaps
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount basisSwapPayFloatingLegDayCount,
      final BusinessDayConvention basisSwapPayFloatingLegBusinessDayConvention, final Frequency basisSwapPayFloatingLegFrequency, final Integer basisSwapPayFloatingLegSettlementDays,
      final ExternalId basisSwapPayFloatingLegInitialRate, final ExternalId basisSwapPayFloatingLegRegion, final DayCount basisSwapReceiveFloatingLegDayCount,
      final BusinessDayConvention basisSwapReceiveFloatingLegBusinessDayConvention, final Frequency basisSwapReceiveFloatingLegFrequency, final Integer basisSwapReceiveFloatingLegSettlementDays,
      final ExternalId basisSwapReceiveFloatingLegInitialRate, final ExternalId basisSwapReceiveFloatingLegRegion) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = null;
    _businessDayConvention = null;
    _frequency = null;
    _settlementDays = null;
    _basisSwapPayFloatingLegDayCount = basisSwapPayFloatingLegDayCount;
    _basisSwapPayFloatingLegBusinessDayConvention = basisSwapPayFloatingLegBusinessDayConvention;
    _basisSwapPayFloatingLegFrequency = basisSwapPayFloatingLegFrequency;
    _basisSwapPayFloatingLegSettlementDays = basisSwapPayFloatingLegSettlementDays;
    _basisSwapPayFloatingLegInitialRate = basisSwapPayFloatingLegInitialRate;
    _basisSwapPayFloatingLegRegion = basisSwapPayFloatingLegRegion;
    _basisSwapReceiveFloatingLegDayCount = basisSwapReceiveFloatingLegDayCount;
    _basisSwapReceiveFloatingLegBusinessDayConvention = basisSwapReceiveFloatingLegBusinessDayConvention;
    _basisSwapReceiveFloatingLegFrequency = basisSwapReceiveFloatingLegFrequency;
    _basisSwapReceiveFloatingLegSettlementDays = basisSwapReceiveFloatingLegSettlementDays;
    _basisSwapReceiveFloatingLegInitialRate = basisSwapReceiveFloatingLegInitialRate;
    _basisSwapReceiveFloatingLegRegion = basisSwapReceiveFloatingLegRegion;
  }

  //equity CAPM
  public ConventionBundleImpl(final String name, final ExternalIdBundle capmRiskFreeRate, final ExternalIdBundle capmMarket) {
    Validate.notNull(name, "name");
    Validate.notNull(capmRiskFreeRate, "CAPM risk free rate");
    Validate.notNull(capmMarket, "CAPM market");
    _name = name;
    _capmRiskFreeRate = capmRiskFreeRate;
    _capmMarket = capmMarket;
  }

  //Bonds
  public ConventionBundleImpl(final String name, final boolean isEOMConvention, final boolean calculateScheduleFromMaturity, final int exDividendDays, final int settlementDays,
      final boolean rollToSettlement) {
    Validate.notNull(name, "name");
    Validate.isTrue(exDividendDays >= 0);
    Validate.isTrue(settlementDays >= 0);
    _name = name;
    _isEOMConvention = isEOMConvention;
    _calculateScheduleFromMaturity = calculateScheduleFromMaturity;
    _exDividendDays = exDividendDays;
    _settlementDays = settlementDays;
    _rollToSettlement = rollToSettlement;
  }

  //Bond future deliverables
  public ConventionBundleImpl(final String name, final boolean isEOMConvention, final boolean calculateScheduleFromMaturity, final int exDividendDays, final int settlementDays,
      final DayCount dayCount, final BusinessDayConvention businessDayConvention, final YieldConvention yieldConvention) {
    Validate.notNull(name, "name");
    Validate.isTrue(exDividendDays >= 0);
    Validate.isTrue(settlementDays >= 0);
    Validate.notNull(dayCount, "day count");
    Validate.notNull(businessDayConvention, "business day convention");
    _name = name;
    _isEOMConvention = isEOMConvention;
    _calculateScheduleFromMaturity = calculateScheduleFromMaturity;
    _exDividendDays = exDividendDays;
    _settlementDays = settlementDays;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _yieldConvention = yieldConvention;
    _rollToSettlement = false;
  }

  //swaptions
  public ConventionBundleImpl(final String name, final boolean isCashSettled) {
    Validate.notNull(name, "name");
    _name = name;
    _isCashSettled = isCashSettled;
  }

  @Override
  public Frequency getFrequency() {
    return _frequency;
  }

  @Override
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  @Override
  public DayCount getDayCount() {
    return _dayCount;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public int getSettlementDays() {
    return _settlementDays;
  }

  @Override
  public ExternalIdBundle getIdentifiers() {
    return _bundle;
  }

  public void setIdentifiers(final ExternalIdBundle updatedBundle) {
    _bundle = updatedBundle;
  }

  @Override
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  public void setUniqueId(final UniqueId uniqueId) {
    _uniqueId = uniqueId;
  }

  /**
   * Gets the swapFixedLegDayCount field.
   * @return the swapFixedLegDayCount
   */
  @Override
  public DayCount getSwapFixedLegDayCount() {
    return _swapFixedLegDayCount;
  }

  /**
   * Gets the swapFixedLegBusinessDayConvention field.
   * @return the swapFixedLegBusinessDayConvention
   */
  @Override
  public BusinessDayConvention getSwapFixedLegBusinessDayConvention() {
    return _swapFixedLegBusinessDayConvention;
  }

  /**
   * Gets the swapFixedLegFrequency field.
   * @return the swapFixedLegFrequency
   */
  @Override
  public Frequency getSwapFixedLegFrequency() {
    return _swapFixedLegFrequency;
  }

  /**
   * Gets the swapFixedLegSettlementDays field.
   * @return the swapFixedLegSettlementDays
   */
  @Override
  public Integer getSwapFixedLegSettlementDays() {
    return _swapFixedLegSettlementDays;
  }

  /**
   * Gets the region identifier for the fixed leg
   * @return the region identifier for the fixed leg
   */
  @Override
  public ExternalId getSwapFixedLegRegion() {
    return _swapFixedLegRegion;
  }

  /**
   * Gets the swapFloatingLegDayCount field.
   * @return the swapFloatingLegDayCount
   */
  @Override
  public DayCount getSwapFloatingLegDayCount() {
    return _swapFloatingLegDayCount;
  }

  /**
   * Gets the swapFloatingLegBusinessDayConvention field.
   * @return the swapFloatingLegBusinessDayConvention
   */
  @Override
  public BusinessDayConvention getSwapFloatingLegBusinessDayConvention() {
    return _swapFloatingLegBusinessDayConvention;
  }

  /**
   * Gets the swapFloatingLegFrequency field.
   * @return the swapFloatingLegFrequency
   */
  @Override
  public Frequency getSwapFloatingLegFrequency() {
    return _swapFloatingLegFrequency;
  }

  /**
   * Gets the swapFloatingLegSettlementDays field.
   * @return the swapFloatingLegSettlementDays
   */
  @Override
  public Integer getSwapFloatingLegSettlementDays() {
    return _swapFloatingLegSettlementDays;
  }

  /**
   * Gets the swapFloatingLegInitialRate field.
   * @return the swapFloatingLegInitialRate
   */
  @Override
  public ExternalId getSwapFloatingLegInitialRate() {
    return _swapFloatingLegInitialRate;
  }

  /**
   * Gets the region identifier for the floating leg
   * @return the region identifier for the floating leg
   */
  @Override
  public ExternalId getSwapFloatingLegRegion() {
    return _swapFloatingLegRegion;
  }

  /**
   * Gets the pointValue field.
   * @return the pointValue
   */
  @Override
  public Double getFutureYearFraction() {
    return _yearFraction;
  }

  /**
   * Gets the name of the risk free rate for CAPM
   * @return the name
   */
  @Override
  public ExternalIdBundle getCAPMRiskFreeRate() {
    return _capmRiskFreeRate;
  }

  /**
   * Gets the name of the market for CAPM
   * @return the name
   */
  @Override
  public ExternalIdBundle getCAPMMarket() {
    return _capmMarket;
  }

  @Override
  public DayCount getBasisSwapPayFloatingLegDayCount() {
    return _basisSwapPayFloatingLegDayCount;
  }

  @Override
  public BusinessDayConvention getBasisSwapPayFloatingLegBusinessDayConvention() {
    return _basisSwapPayFloatingLegBusinessDayConvention;
  }

  @Override
  public Frequency getBasisSwapPayFloatingLegFrequency() {
    return _basisSwapPayFloatingLegFrequency;
  }

  @Override
  public Integer getBasisSwapPayFloatingLegSettlementDays() {
    return _basisSwapPayFloatingLegSettlementDays;
  }

  @Override
  public ExternalId getBasisSwapPayFloatingLegInitialRate() {
    return _basisSwapPayFloatingLegInitialRate;
  }

  @Override
  public ExternalId getBasisSwapPayFloatingLegRegion() {
    return _basisSwapPayFloatingLegRegion;
  }

  @Override
  public DayCount getBasisSwapReceiveFloatingLegDayCount() {
    return _basisSwapReceiveFloatingLegDayCount;
  }

  @Override
  public BusinessDayConvention getBasisSwapReceiveFloatingLegBusinessDayConvention() {
    return _basisSwapReceiveFloatingLegBusinessDayConvention;
  }

  @Override
  public Frequency getBasisSwapReceiveFloatingLegFrequency() {
    return _basisSwapReceiveFloatingLegFrequency;
  }

  @Override
  public Integer getBasisSwapReceiveFloatingLegSettlementDays() {
    return _basisSwapReceiveFloatingLegSettlementDays;
  }

  @Override
  public ExternalId getBasisSwapReceiveFloatingLegInitialRate() {
    return _basisSwapReceiveFloatingLegInitialRate;
  }

  @Override
  public ExternalId getBasisSwapReceiveFloatingLegRegion() {
    return _basisSwapReceiveFloatingLegRegion;
  }

  @Override
  public Boolean isEOMConvention() {
    return _isEOMConvention;
  }

  @Override
  public Boolean calculateScheduleFromMaturity() {
    return _calculateScheduleFromMaturity;
  }

  @Override
  public int getExDividendDays() {
    return _exDividendDays;
  }

  @Override
  public YieldConvention getYieldConvention() {
    return _yieldConvention;
  }

  @Override
  public boolean rollToSettlement() {
    return _rollToSettlement;
  }

  @Override
  public Period getPeriod() {
    return _period;
  }

  @Override
  public boolean isCashSettled() {
    return _isCashSettled;
  }

  @Override
  public ExternalId getRegion() {
    return _region;
  }

  @Override
  public Integer getOvernightIndexSwapPublicationLag() {
    return _publicationLag;
  }
}
