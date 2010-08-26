/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

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
  private String _name;
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
  
  public ConventionBundleImpl(IdentifierBundle initialBundle, String name, DayCount dayCount, BusinessDayConvention businessDayConvention, Frequency frequency, int settlementDays) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _frequency = frequency;
    _settlementDays = settlementDays;
  }
  
  public ConventionBundleImpl(IdentifierBundle initialBundle, String name, 
                              DayCount swapFixedLegDayCount, BusinessDayConvention swapFixedLegBusinessDayConvention, 
                              Frequency swapFixedLegFrequency, Integer swapFixedLegSettlementDays, 
                              DayCount swapFloatingLegDayCount, BusinessDayConvention swapFloatingLegBusinessDayConvention, 
                              Frequency swapFloatingLegFrequency, Integer swapFloatingLegSettlementDays, Identifier swapFloatingLegInitialRate) {
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
  
  public void setIdentifiers(IdentifierBundle updatedBundle) {
    _bundle = updatedBundle;
  }

  @Override
  public UniqueIdentifier getUniqueIdentifier() {
    return _uniqueIdentifier;
  }
  
  public void setUniqueIdentifier(UniqueIdentifier uniqueIdentifier) {
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
  
  
}
