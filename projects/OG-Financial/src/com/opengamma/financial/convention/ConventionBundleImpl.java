/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * An implementation of ReferenceRate for use by the InMemoryReferenceRateRepository.  Note it is NOT immutable, because the repository needs to be able to assign 
 * unique ids to it retrospectively, and to be able to update the bundle.
 */
public class ConventionBundleImpl implements ConventionBundle {
  private UniqueIdentifier _uniqueIdentifier;
  private IdentifierBundle _bundle;
  private final String _name;
  private DayCount _dayCount;
  private BusinessDayConvention _businessDayConvention;
  private Integer _settlementDays;
  private Frequency _frequency;

  private DayCount _swapFixedLegDayCount;
  private BusinessDayConvention _swapFixedLegBusinessDayConvention;
  private Frequency _swapFixedLegFrequency;
  private Integer _swapFixedLegSettlementDays;
  private DayCount _swapFloatingLegDayCount;
  private BusinessDayConvention _swapFloatingLegBusinessDayConvention;
  private Frequency _swapFloatingLegFrequency;
  private Integer _swapFloatingLegSettlementDays;
  private Identifier _swapFloatingLegInitialRate;
  private Double _pointValue;

  //Equity models
  private String _capmRiskFreeRateName;
  private String _capmMarketName;

  // cash/general
  public ConventionBundleImpl(final IdentifierBundle initialBundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention,
      final Frequency frequency, final int settlementDays) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _frequency = frequency;
    _settlementDays = settlementDays;
  }

  // futures
  public ConventionBundleImpl(final IdentifierBundle initialBundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention,
      final Frequency frequency, final int settlementDays, final double pointValue) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _frequency = frequency;
    _settlementDays = settlementDays;
    _pointValue = pointValue;
  }

  // swaps
  public ConventionBundleImpl(final IdentifierBundle initialBundle, final String name, final DayCount swapFixedLegDayCount,
      final BusinessDayConvention swapFixedLegBusinessDayConvention, final Frequency swapFixedLegFrequency, final Integer swapFixedLegSettlementDays,
      final DayCount swapFloatingLegDayCount, final BusinessDayConvention swapFloatingLegBusinessDayConvention, final Frequency swapFloatingLegFrequency,
      final Integer swapFloatingLegSettlementDays, final Identifier swapFloatingLegInitialRate) {
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
    _swapFloatingLegDayCount = swapFloatingLegDayCount;
    _swapFloatingLegBusinessDayConvention = swapFloatingLegBusinessDayConvention;
    _swapFloatingLegFrequency = swapFloatingLegFrequency;
    _swapFloatingLegSettlementDays = swapFloatingLegSettlementDays;
    _swapFloatingLegInitialRate = swapFloatingLegInitialRate;
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
  public UniqueIdentifier getUniqueIdentifier() {
    return _uniqueIdentifier;
  }

  public void setUniqueIdentifier(final UniqueIdentifier uniqueIdentifier) {
    _uniqueIdentifier = uniqueIdentifier;
  }

  /**
   * Gets the swapFixedLegDayCount field.
   * @return the swapFixedLegDayCount
   */
  public DayCount getSwapFixedLegDayCount() {
    return _swapFixedLegDayCount;
  }

  /**
   * Gets the swapFixedLegBusinessDayConvention field.
   * @return the swapFixedLegBusinessDayConvention
   */
  public BusinessDayConvention getSwapFixedLegBusinessDayConvention() {
    return _swapFixedLegBusinessDayConvention;
  }

  /**
   * Gets the swapFixedLegFrequency field.
   * @return the swapFixedLegFrequency
   */
  public Frequency getSwapFixedLegFrequency() {
    return _swapFixedLegFrequency;
  }

  /**
   * Gets the swapFixedLegSettlementDays field.
   * @return the swapFixedLegSettlementDays
   */
  public Integer getSwapFixedLegSettlementDays() {
    return _swapFixedLegSettlementDays;
  }

  /**
   * Gets the swapFloatingLegDayCount field.
   * @return the swapFloatingLegDayCount
   */
  public DayCount getSwapFloatingLegDayCount() {
    return _swapFloatingLegDayCount;
  }

  /**
   * Gets the swapFloatingLegBusinessDayConvention field.
   * @return the swapFloatingLegBusinessDayConvention
   */
  public BusinessDayConvention getSwapFloatingLegBusinessDayConvention() {
    return _swapFloatingLegBusinessDayConvention;
  }

  /**
   * Gets the swapFloatingLegFrequency field.
   * @return the swapFloatingLegFrequency
   */
  public Frequency getSwapFloatingLegFrequency() {
    return _swapFloatingLegFrequency;
  }

  /**
   * Gets the swapFloatingLegSettlementDays field.
   * @return the swapFloatingLegSettlementDays
   */
  public Integer getSwapFloatingLegSettlementDays() {
    return _swapFloatingLegSettlementDays;
  }

  /**
   * Gets the swapFloatingLegInitialRate field.
   * @return the swapFloatingLegInitialRate
   */
  public Identifier getSwapFloatingLegInitialRate() {
    return _swapFloatingLegInitialRate;
  }

  /**
   * Gets the pointValue field.
   * @return the pointValue
   */
  public Double getFuturePointValue() {
    return _pointValue;
  }

  /**
   * Gets the name of the risk free rate for CAPM
   * @return the name
   */
  public String getCAPMRiskFreeRateName() {
    return _capmRiskFreeRateName;
  }

  /**
   * Gets the name of the market for CAPM
   * @return the name
   */
  public String getCAPMMarketName() {
    return _capmMarketName;
  }
}
