/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * An implementation of ReferenceRate for use by the InMemoryReferenceRateRepository.  Note it is NOT immutable, because the repository needs to be able to assign 
 * unique ids to it retrospectively, and to be able to update the bundle.
 */
public class ReferenceRateImpl implements ReferenceRate {
  private UniqueIdentifier _uniqueIdentifier;
  private IdentifierBundle _bundle;
  private String _name;
  private DayCount _dayCount;
  private BusinessDayConvention _businessDayConvention;
  private int _settlementDays;
  private Frequency _frequency;

  public ReferenceRateImpl(IdentifierBundle initialBundle, String name, DayCount dayCount, BusinessDayConvention businessDayConvention, Frequency frequency, int settlementDays) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _frequency = frequency;
    _settlementDays = settlementDays;
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
}
