/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.Region;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.id.UniqueIdentifier;

/**
 * Represents a floating interest rate leg of a swap.
 */
public class FloatingInterestRateLeg extends InterestRateLeg {
  private final UniqueIdentifier _floatingReferenceRateIdentifier;
  private final double _initialFloatingRate;
  private final double _spread;

  /**
   * @param dayCount The daycount
   * @param frequency The payment frequency
   * @param region The region
   * @param businessDayConvention The business day convention
   * @param notional The notional
   * @param floatingReferenceRateIdentifier the unique id of the object used to provide the floating rate
   * @param initialFloatingRate The floating rate of the first period of the swap (expressed as a decimal)
   */
  public FloatingInterestRateLeg(final DayCount dayCount, final Frequency frequency, final Region region, final BusinessDayConvention businessDayConvention, final InterestRateNotional notional,
      final UniqueIdentifier floatingReferenceRateIdentifier, final double initialFloatingRate) {
    this(dayCount, frequency, region, businessDayConvention, notional, floatingReferenceRateIdentifier, initialFloatingRate, 0);
  }

  /**
   * @param dayCount The daycount
   * @param frequency The payment frequency
   * @param region The region
   * @param businessDayConvention The business day convention
   * @param notional The notional
   * @param floatingReferenceRateIdentifier the unique id of the object used to provide the floating rate
   * @param initialFloatingRate The floating rate of the first period of the swap (expressed as a decimal)
   * @param spread The spread over the floating reference rate that is to be used (expressed as a decimal)
   */
  public FloatingInterestRateLeg(final DayCount dayCount, final Frequency frequency, final Region region, final BusinessDayConvention businessDayConvention, final InterestRateNotional notional,
      final UniqueIdentifier floatingReferenceRateIdentifier, final double initialFloatingRate, final double spread) {
    super(dayCount, frequency, region, businessDayConvention, notional);
    Validate.notNull(floatingReferenceRateIdentifier);
    _floatingReferenceRateIdentifier = floatingReferenceRateIdentifier;
    _initialFloatingRate = initialFloatingRate;
    _spread = spread;
  }

  /**
   * @return the unique id of the object used to provide the floating rate
   */
  public UniqueIdentifier getFloatingIdentifier() {
    return _floatingReferenceRateIdentifier;
  }

  public double getInitialFloatingRate() {
    return _initialFloatingRate;
  }

  public double getSpread() {
    return _spread;
  }
}
