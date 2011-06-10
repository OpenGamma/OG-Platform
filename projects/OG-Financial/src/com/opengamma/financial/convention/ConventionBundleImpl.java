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
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * An implementation of ReferenceRate for use by the InMemoryReferenceRateRepository.  Note it is NOT immutable, because the repository needs to be able to assign 
 * unique ids to it retrospectively, and to be able to update the bundle.
 */
public class ConventionBundleImpl implements ConventionBundle {

  private UniqueIdentifier _uniqueId;
  private IdentifierBundle _bundle;
  private final String _name;
  private DayCount _dayCount;
  private BusinessDayConvention _businessDayConvention;
  private Integer _settlementDays;
  private Frequency _frequency;
  private Period _period;

  private DayCount _swapFixedLegDayCount;
  private BusinessDayConvention _swapFixedLegBusinessDayConvention;
  private Frequency _swapFixedLegFrequency;
  private Integer _swapFixedLegSettlementDays;
  private DayCount _swapFloatingLegDayCount;
  private BusinessDayConvention _swapFloatingLegBusinessDayConvention;
  private Frequency _swapFloatingLegFrequency;
  private Integer _swapFloatingLegSettlementDays;
  private Identifier _swapFloatingLegInitialRate;
  private Double _yearFraction;

  //Equity models
  private String _capmRiskFreeRateName;
  private String _capmMarketName;

  // basis swaps
  private DayCount _basisSwapPayFloatingLegDayCount;
  private BusinessDayConvention _basisSwapPayFloatingLegBusinessDayConvention;
  private Frequency _basisSwapPayFloatingLegFrequency;
  private Integer _basisSwapPayFloatingLegSettlementDays;
  private Identifier _basisSwapPayFloatingLegInitialRate;
  private Identifier _basisSwapPayFloatingLegRegion;
  private DayCount _basisSwapReceiveFloatingLegDayCount;
  private BusinessDayConvention _basisSwapReceiveFloatingLegBusinessDayConvention;
  private Frequency _basisSwapReceiveFloatingLegFrequency;
  private Integer _basisSwapReceiveFloatingLegSettlementDays;
  private Identifier _basisSwapReceiveFloatingLegInitialRate;
  private Identifier _basisSwapReceiveFloatingLegRegion;
  private Identifier _swapFixedLegRegion;
  private Identifier _swapFloatingLegRegion;

  // bonds
  private boolean _isEOMConvention;
  private boolean _calculateScheduleFromMaturity;
  private int _exDividendDays;
  private YieldConvention _yieldConvention;
  private boolean _rollToSettlement;

  // cash/general
  public ConventionBundleImpl(final IdentifierBundle initialBundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final Frequency frequency,
      final int settlementDays) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _frequency = frequency;
    _settlementDays = settlementDays;
  }

  // cash/general
  public ConventionBundleImpl(final IdentifierBundle initialBundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final Period period,
      final int settlementDays) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _period = period;
    _settlementDays = settlementDays;
  }

  // futures
  public ConventionBundleImpl(final IdentifierBundle initialBundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final Frequency frequency,
      final int settlementDays, final double yearFraction) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _frequency = frequency;
    _settlementDays = settlementDays;
    _yearFraction = yearFraction;
  }

  // swaps
  public ConventionBundleImpl(final IdentifierBundle initialBundle, final String name, final DayCount swapFixedLegDayCount, final BusinessDayConvention swapFixedLegBusinessDayConvention,
      final Frequency swapFixedLegFrequency, final Integer swapFixedLegSettlementDays, final Identifier swapFixedLegRegion, final DayCount swapFloatingLegDayCount,
      final BusinessDayConvention swapFloatingLegBusinessDayConvention, final Frequency swapFloatingLegFrequency, final Integer swapFloatingLegSettlementDays,
      final Identifier swapFloatingLegInitialRate, final Identifier swapFloatingLegRegion) {
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

  // basis swaps
  public ConventionBundleImpl(final IdentifierBundle initialBundle, final String name, final DayCount basisSwapPayFloatingLegDayCount,
      final BusinessDayConvention basisSwapPayFloatingLegBusinessDayConvention, final Frequency basisSwapPayFloatingLegFrequency, final Integer basisSwapPayFloatingLegSettlementDays,
      final Identifier basisSwapPayFloatingLegInitialRate, final Identifier basisSwapPayFloatingLegRegion, final DayCount basisSwapReceiveFloatingLegDayCount,
      final BusinessDayConvention basisSwapReceiveFloatingLegBusinessDayConvention, final Frequency basisSwapReceiveFloatingLegFrequency, final Integer basisSwapReceiveFloatingLegSettlementDays,
      final Identifier basisSwapReceiveFloatingLegInitialRate, final Identifier basisSwapReceiveFloatingLegRegion) {
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
  public ConventionBundleImpl(final String name, final String capmRiskFreeRateName, final String capmMarketName) {
    Validate.notNull(name, "name");
    Validate.notNull(capmRiskFreeRateName, "CAPM risk free rate name");
    Validate.notNull(capmMarketName, "CAPM market name");
    _name = name;
    _capmRiskFreeRateName = capmRiskFreeRateName;
    _capmMarketName = capmMarketName;
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
  public IdentifierBundle getIdentifiers() {
    return _bundle;
  }

  public void setIdentifiers(final IdentifierBundle updatedBundle) {
    _bundle = updatedBundle;
  }

  @Override
  public UniqueIdentifier getUniqueId() {
    return _uniqueId;
  }

  public void setUniqueId(final UniqueIdentifier uniqueId) {
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
  public Identifier getSwapFixedLegRegion() {
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
  public Identifier getSwapFloatingLegInitialRate() {
    return _swapFloatingLegInitialRate;
  }

  /**
   * Gets the region identifier for the floating leg
   * @return the region identifier for the floating leg
   */
  @Override
  public Identifier getSwapFloatingLegRegion() {
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
  public String getCAPMRiskFreeRateName() {
    return _capmRiskFreeRateName;
  }

  /**
   * Gets the name of the market for CAPM
   * @return the name
   */
  @Override
  public String getCAPMMarketName() {
    return _capmMarketName;
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
  public Identifier getBasisSwapPayFloatingLegInitialRate() {
    return _basisSwapPayFloatingLegInitialRate;
  }

  @Override
  public Identifier getBasisSwapPayFloatingLegRegion() {
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
  public Identifier getBasisSwapReceiveFloatingLegInitialRate() {
    return _basisSwapReceiveFloatingLegInitialRate;
  }

  @Override
  public Identifier getBasisSwapReceiveFloatingLegRegion() {
    return _basisSwapReceiveFloatingLegRegion;
  }

  @Override
  public boolean isEOMConvention() {
    return _isEOMConvention;
  }

  @Override
  public boolean calculateScheduleFromMaturity() {
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
}
