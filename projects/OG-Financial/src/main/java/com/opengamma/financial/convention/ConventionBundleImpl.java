/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.io.Serializable;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.interestrate.InterestRate;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * An implementation of ConventionBundle for use by the InMemoryConventionBundleMaster.  Note it is NOT immutable, because the master needs to be able to assign
 * UniqueIds to it retrospectively, and to be able to update the bundle.
 */
public class ConventionBundleImpl implements ConventionBundle, Serializable {

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
  private Frequency _swapFixedLegCompoundingFrequency;
  private InterestRate.Type _swapFixedLegCompoundingType;
  private DayCount _swapFloatingLegDayCount;
  private BusinessDayConvention _swapFloatingLegBusinessDayConvention;
  private Frequency _swapFloatingLegPaymentFrequency;
  private Integer _swapFloatingLegSettlementDays;
  private ExternalId _swapFloatingLegInitialRate;
  private Frequency _swapFloatingLegCompoundingFrequency;
  private InterestRate.Type _swapFloatingLegCompoundingType;
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
  private int _shortSettlementDays;
  private int _longSettlementDays;
  private Tenor _cutoffTenor;

  //swaptions
  private boolean _isCashSettled;

  //option expiries on exchanges
  private String _optionExpiryCalculator;

  /**
   * Constructor to create a convention bundle for cash/general types
   * @param initialBundle the bundle of ids associated with the type
   * @param name the descriptive name of the type
   * @param dayCount the day count convention
   * @param businessDayConvention the business day convention
   * @param frequency the frequency
   * @param settlementDays the number of days to settle
   */
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention,
      final Frequency frequency, final int settlementDays) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _frequency = frequency;
    _settlementDays = settlementDays;
  }

  /**
   * Constructor to create a convention bundle for generic cash (no frequency applicable)
   * @param initialBundle the bundle of ExternalIds associated with the type
   * @param name the descriptive name of the type
   * @param dayCount the day count convention
   * @param businessDayConvention the business day convention
   * @param settlementDays the number of days to settle
   */
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention,
      final int settlementDays) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _settlementDays = settlementDays;
  }

  /**
   * Constructor to create a convention bundle for cash/general (includes a period and region)
   * @param initialBundle the bundle of ExternalIds associated with the type
   * @param name the descriptive name of the type
   * @param dayCount the day count convention
   * @param businessDayConvention the business day convention
   * @param period the period
   * @param settlementDays the number of days to settle
   * @param region the ExternalId of the region associated with this type
   */
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention,
      final Period period, final int settlementDays, final ExternalId region) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _period = period;
    _settlementDays = settlementDays;
    _region = region;
  }

  /**
   * Constructor to create a convention bundle for cash/general where EOM is indicated
   * @param initialBundle the bundle of ExternalIds associated with the type
   * @param name the descriptive name of the type
   * @param dayCount the day count convention
   * @param businessDayConvention the business day convention
   * @param period the period
   * @param settlementDays the number of days to settle
   * @param isEOM whether the End-of-month convention is used in schedule generation
   * @param region the ExternalId of the region associated with this type
   */
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention,
      final Period period, final int settlementDays, final boolean isEOM, final ExternalId region) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _period = period;
    _settlementDays = settlementDays;
    _isEOMConvention = isEOM;
    _region = region;
  }

  /**
   * Constructor to create a convention bundle for *IBOR indices (e.g. floating reference rate for swaps)
   * @param initialBundle the bundle of ExternalIds associated with the type
   * @param name the descriptive name of the type
   * @param dayCount the day count convention
   * @param businessDayConvention the business day convention
   * @param settlementDays the number of days to settle
   * @param isEOMConvention whether the End-of-month convention is used in schedule generation
   */
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention,
      final int settlementDays, final boolean isEOMConvention) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _settlementDays = settlementDays;
    _isEOMConvention = isEOMConvention;
  }

  /**
   * Constructor to create a convention bundle for *IBOR indices (e.g. floating reference rate for swaps)
   * @param bundle the bundle of ExternalIds associated with the type
   * @param name the descriptive name of the type
   * @param dayCount the day count convention
   * @param businessDayConvention the business day convention
   * @param period the tenor of the index (e.g. 3M)
   * @param settlementDays the number of days to settle
   * @param isEOM whether the End-of-month convention is used in schedule generation
   * @param region the ExternalId of the region associated with this type
   * @param publicationLag the lag in publication from start of the period to publication of the index (e.g. USD = 1, most 0)
   */
  public ConventionBundleImpl(final ExternalIdBundle bundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention,
      final Period period, final int settlementDays, final boolean isEOM, final ExternalId region, final Integer publicationLag) {
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

  /**
   * Constructor to create a convention bundle for futures
   * @param initialBundle the bundle of ExternalIds associated with the type
   * @param name the descriptive name of the type
   * @param dayCount the day count convention
   * @param businessDayConvention the business day convention
   * @param frequency the frequency
   * @param settlementDays the number of days to settle
   * @param yearFraction the year fraction
   */
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount dayCount, final BusinessDayConvention businessDayConvention,
      final Frequency frequency, final int settlementDays, final double yearFraction) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _frequency = frequency;
    _settlementDays = settlementDays;
    _yearFraction = yearFraction;
  }

  /**
   * Constructor to create a convention bundle for Swaps and FRA without end-of-month (EOM) convention flag
   * @param initialBundle the bundle of ExternalIds associated with the type
   * @param name the descriptive name of the type
   * @param swapFixedLegDayCount the day count convention of the fixed leg
   * @param swapFixedLegBusinessDayConvention the business day convention of the fixed leg
   * @param swapFixedLegFrequency the frequency of the fixed leg
   * @param swapFixedLegSettlementDays the number of days to settle on the fixed leg
   * @param swapFixedLegRegion the ExternalId referencing the region associated with the fixed leg
   * @param swapFloatingLegDayCount the day count convention associated with the floating leg
   * @param swapFloatingLegBusinessDayConvention the business day convention associated with the floating leg
   * @param swapFloatingLegFrequency the frequency associated with the floating leg
   * @param swapFloatingLegSettlementDays the number of days to settle on the floating leg
   * @param swapFloatingLegInitialRate the initial rate of the floating leg
   * @param swapFloatingLegRegion the ExternalId referencing the region associated with the floating leg
   */
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount swapFixedLegDayCount,
      final BusinessDayConvention swapFixedLegBusinessDayConvention, final Frequency swapFixedLegFrequency, final Integer swapFixedLegSettlementDays,
      final ExternalId swapFixedLegRegion, final DayCount swapFloatingLegDayCount, final BusinessDayConvention swapFloatingLegBusinessDayConvention,
      final Frequency swapFloatingLegFrequency, final Integer swapFloatingLegSettlementDays, final ExternalId swapFloatingLegInitialRate,
      final ExternalId swapFloatingLegRegion) {
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
    _swapFloatingLegPaymentFrequency = swapFloatingLegFrequency;
    _swapFloatingLegSettlementDays = swapFloatingLegSettlementDays;
    _swapFloatingLegInitialRate = swapFloatingLegInitialRate;
    _swapFloatingLegRegion = swapFloatingLegRegion;
  }

  /**
   * Constructor to create a convention bundle for Swaps and FRA with end-of-month (EOM) convention flag
   * @param initialBundle the bundle of ExternalIds associated with the type
   * @param name the descriptive name of the type
   * @param swapFixedLegDayCount the day count convention of the fixed leg
   * @param swapFixedLegBusinessDayConvention the business day convention of the fixed leg
   * @param swapFixedLegFrequency the frequency of the fixed leg
   * @param swapFixedLegSettlementDays the number of days to settle on the fixed leg
   * @param swapFixedLegRegion the ExternalId referencing the region associated with the fixed leg
   * @param swapFloatingLegDayCount the day count convention associated with the floating leg
   * @param swapFloatingLegBusinessDayConvention the business day convention associated with the floating leg
   * @param swapFloatingLegFrequency the frequency associated with the floating leg
   * @param swapFloatingLegSettlementDays the number of days to settle on the floating leg
   * @param swapFloatingLegInitialRate the initial rate of the floating leg
   * @param swapFloatingLegRegion the ExternalId referencing the region associated with the floating leg
   * @param isEOM whether the swap or FRA should follow the end-of-month convention when calculating schedules
   */
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount swapFixedLegDayCount,
      final BusinessDayConvention swapFixedLegBusinessDayConvention, final Frequency swapFixedLegFrequency, final Integer swapFixedLegSettlementDays,
      final ExternalId swapFixedLegRegion, final DayCount swapFloatingLegDayCount, final BusinessDayConvention swapFloatingLegBusinessDayConvention,
      final Frequency swapFloatingLegFrequency, final Integer swapFloatingLegSettlementDays, final ExternalId swapFloatingLegInitialRate,
      final ExternalId swapFloatingLegRegion, final boolean isEOM) {
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
    _swapFloatingLegPaymentFrequency = swapFloatingLegFrequency;
    _swapFloatingLegSettlementDays = swapFloatingLegSettlementDays;
    _swapFloatingLegInitialRate = swapFloatingLegInitialRate;
    _swapFloatingLegRegion = swapFloatingLegRegion;
    _isEOMConvention = isEOM;
  }

  /**
   * Constructor to create a convention bundle for Swap Indices with end-of-month (EOM) convention flag and swap tenor.
   * The payment frequency and compounding frequency of both legs is assumed to be the same, and the compounding
   * type of both legs is assumed to be continuous.
   * @param initialBundle the bundle of ExternalIds associated with the type
   * @param name the descriptive name of the type
   * @param swapFixedLegDayCount the day count convention of the fixed leg
   * @param swapFixedLegBusinessDayConvention the business day convention of the fixed leg
   * @param swapFixedLegFrequency the frequency of the fixed leg
   * @param swapFixedLegSettlementDays the number of days to settle on the fixed leg
   * @param swapFixedLegRegion the ExternalId referencing the region associated with the fixed leg
   * @param swapFloatingLegDayCount the day count convention associated with the floating leg
   * @param swapFloatingLegBusinessDayConvention the business day convention associated with the floating leg
   * @param swapFloatingLegFrequency the frequency associated with the floating leg
   * @param swapFloatingLegSettlementDays the number of days to settle on the floating leg
   * @param swapFloatingLegInitialRate the initial rate of the floating leg
   * @param swapFloatingLegRegion the ExternalId referencing the region associated with the floating leg
   * @param isEOM whether the swap or FRA should follow the end-of-month convention when calculating schedules
   * @param swapTenor the period of the swap (tenor)
   */
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount swapFixedLegDayCount,
      final BusinessDayConvention swapFixedLegBusinessDayConvention, final Frequency swapFixedLegFrequency, final Integer swapFixedLegSettlementDays,
      final ExternalId swapFixedLegRegion, final DayCount swapFloatingLegDayCount, final BusinessDayConvention swapFloatingLegBusinessDayConvention,
      final Frequency swapFloatingLegFrequency, final Integer swapFloatingLegSettlementDays, final ExternalId swapFloatingLegInitialRate,
      final ExternalId swapFloatingLegRegion, final boolean isEOM, final Period swapTenor) {
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
    _swapFloatingLegPaymentFrequency = swapFloatingLegFrequency;
    _swapFloatingLegSettlementDays = swapFloatingLegSettlementDays;
    _swapFloatingLegInitialRate = swapFloatingLegInitialRate;
    _swapFloatingLegRegion = swapFloatingLegRegion;
    _isEOMConvention = isEOM;
    _period = swapTenor;
  }

  /**
   * Constructor to create a convention bundle for Swap Indices with end-of-month (EOM) convention flag
   * and the compounding frequency and type of the fixed and float legs.
   * @param initialBundle the bundle of ExternalIds associated with the type
   * @param name the descriptive name of the type
   * @param swapFixedLegDayCount the day count convention of the fixed leg
   * @param swapFixedLegBusinessDayConvention the business day convention of the fixed leg
   * @param swapFixedLegPaymentFrequency the payment frequency of the fixed leg
   * @param swapFixedLegSettlementDays the number of days to settle on the fixed leg
   * @param swapFixedLegRegion the ExternalId referencing the region associated with the fixed leg
   * @param swapFixedLegCompoundingFrequency the compounding frequency of the fixed leg
   * @param swapFixedLegCompoundingType the compounding type of the fixed leg
   * @param swapFloatingLegDayCount the day count convention associated with the floating leg
   * @param swapFloatingLegBusinessDayConvention the business day convention associated with the floating leg
   * @param swapFloatingLegPaymentFrequency the payment frequency associated with the floating leg
   * @param swapFloatingLegSettlementDays the number of days to settle on the floating leg
   * @param swapFloatingLegInitialRate the initial rate of the floating leg
   * @param swapFloatingLegRegion the ExternalId referencing the region associated with the floating leg
   * @param swapFloatingLegCompoundingFrequency the compounding frequency of the floating leg
   * @param swapFloatingLegCompoundingType the compounding type of the floating leg
   * @param isEOM whether the swap or FRA should follow the end-of-month convention when calculating schedules
   */
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount swapFixedLegDayCount,
      final BusinessDayConvention swapFixedLegBusinessDayConvention, final Frequency swapFixedLegPaymentFrequency, final Integer swapFixedLegSettlementDays,
      final ExternalId swapFixedLegRegion, final Frequency swapFixedLegCompoundingFrequency, final InterestRate.Type swapFixedLegCompoundingType,
      final DayCount swapFloatingLegDayCount, final BusinessDayConvention swapFloatingLegBusinessDayConvention, final Frequency swapFloatingLegPaymentFrequency,
      final Integer swapFloatingLegSettlementDays, final ExternalId swapFloatingLegInitialRate, final ExternalId swapFloatingLegRegion,
      final Frequency swapFloatingLegCompoundingFrequency, final InterestRate.Type swapFloatingLegCompoundingType, final boolean isEOM) {
    _bundle = initialBundle;
    _name = name;
    _dayCount = null;
    _businessDayConvention = null;
    _frequency = null;
    _settlementDays = null;
    _swapFixedLegDayCount = swapFixedLegDayCount;
    _swapFixedLegBusinessDayConvention = swapFixedLegBusinessDayConvention;
    _swapFixedLegFrequency = swapFixedLegPaymentFrequency;
    _swapFixedLegSettlementDays = swapFixedLegSettlementDays;
    _swapFixedLegRegion = swapFixedLegRegion;
    _swapFixedLegCompoundingFrequency = swapFixedLegCompoundingFrequency;
    _swapFixedLegCompoundingType = swapFixedLegCompoundingType;
    _swapFloatingLegDayCount = swapFloatingLegDayCount;
    _swapFloatingLegBusinessDayConvention = swapFloatingLegBusinessDayConvention;
    _swapFloatingLegPaymentFrequency = swapFloatingLegPaymentFrequency;
    _swapFloatingLegSettlementDays = swapFloatingLegSettlementDays;
    _swapFloatingLegInitialRate = swapFloatingLegInitialRate;
    _swapFloatingLegRegion = swapFloatingLegRegion;
    _swapFloatingLegCompoundingFrequency = swapFloatingLegCompoundingFrequency;
    _swapFloatingLegCompoundingType = swapFloatingLegCompoundingType;
    _isEOMConvention = isEOM;
  }

  /**
   * Constructor to create a convention bundle for OIS Swaps with end-of-month (EOM) convention flag and publication lag
   * @param initialBundle the bundle of ExternalIds associated with the type
   * @param name the descriptive name of the type
   * @param swapFixedLegDayCount the day count convention of the fixed leg
   * @param swapFixedLegBusinessDayConvention the business day convention of the fixed leg
   * @param swapFixedLegFrequency the frequency of the fixed leg
   * @param swapFixedLegSettlementDays the number of days to settle on the fixed leg
   * @param swapFixedLegRegion the ExternalId referencing the region associated with the fixed leg
   * @param swapFloatingLegDayCount the day count convention associated with the floating leg
   * @param swapFloatingLegBusinessDayConvention the business day convention associated with the floating leg
   * @param swapFloatingLegFrequency the frequency associated with the floating leg
   * @param swapFloatingLegSettlementDays the number of days to settle on the floating leg
   * @param swapFloatingLegInitialRate the initial rate of the floating leg
   * @param swapFloatingLegRegion the ExternalId referencing the region associated with the floating leg
   * @param isEOM whether the swap or FRA should follow the end-of-month convention when calculating schedules
   * @param publicationLag the lag in publication from start of the period to publication of the index (e.g. USD = 1, most 0)
   */
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount swapFixedLegDayCount,
      final BusinessDayConvention swapFixedLegBusinessDayConvention, final Frequency swapFixedLegFrequency, final Integer swapFixedLegSettlementDays,
      final ExternalId swapFixedLegRegion, final DayCount swapFloatingLegDayCount, final BusinessDayConvention swapFloatingLegBusinessDayConvention,
      final Frequency swapFloatingLegFrequency, final Integer swapFloatingLegSettlementDays, final ExternalId swapFloatingLegInitialRate,
      final ExternalId swapFloatingLegRegion, final boolean isEOM, final Integer publicationLag) {
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
    _swapFloatingLegPaymentFrequency = swapFloatingLegFrequency;
    _swapFloatingLegSettlementDays = swapFloatingLegSettlementDays;
    _swapFloatingLegInitialRate = swapFloatingLegInitialRate;
    _swapFloatingLegRegion = swapFloatingLegRegion;
    _isEOMConvention = isEOM;
    _publicationLag = publicationLag;
  }

  /**
   * Constructor to create a convention bundle for Basis Swaps
   * @param initialBundle the bundle of ExternalIds associated with the type
   * @param name the descriptive name of the type
   * @param basisSwapPayFloatingLegDayCount the day count convention of the pay floating leg
   * @param basisSwapPayFloatingLegBusinessDayConvention the business day convention of the pay floating leg
   * @param basisSwapPayFloatingLegFrequency the frequency of the pay floating leg
   * @param basisSwapPayFloatingLegSettlementDays the number of days to settle on the pay floating leg
   * @param basisSwapPayFloatingLegInitialRate the initial rate of the pay floating leg
   * @param basisSwapPayFloatingLegRegion the ExternalId referencing the region associated with the pay floating leg
   * @param basisSwapReceiveFloatingLegDayCount the day count convention associated with the receive floating leg
   * @param basisSwapReceiveFloatingLegBusinessDayConvention the business day convention associated with the receive floating leg
   * @param basisSwapReceiveFloatingLegFrequency the frequency associated with the receive floating leg
   * @param basisSwapReceiveFloatingLegSettlementDays the number of days to settle on the receive floating leg
   * @param basisSwapReceiveFloatingLegInitialRate the initial rate of the receive floating leg
   * @param basisSwapReceiveFloatingLegRegion the ExternalId referencing the region associated with the receive floating leg
   */
  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final DayCount basisSwapPayFloatingLegDayCount,
      final BusinessDayConvention basisSwapPayFloatingLegBusinessDayConvention, final Frequency basisSwapPayFloatingLegFrequency,
      final Integer basisSwapPayFloatingLegSettlementDays, final ExternalId basisSwapPayFloatingLegInitialRate, final ExternalId basisSwapPayFloatingLegRegion,
      final DayCount basisSwapReceiveFloatingLegDayCount, final BusinessDayConvention basisSwapReceiveFloatingLegBusinessDayConvention,
      final Frequency basisSwapReceiveFloatingLegFrequency, final Integer basisSwapReceiveFloatingLegSettlementDays,
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

  /**
   * Constructor to create a convention bundle for Equity CAPM
   * @param name the descriptive name of the type
   * @param capmRiskFreeRate the CAPM risk free rate
   * @param capmMarket the CAPM market
   */
  public ConventionBundleImpl(final String name, final ExternalIdBundle capmRiskFreeRate, final ExternalIdBundle capmMarket) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(capmRiskFreeRate, "CAPM risk free rate");
    ArgumentChecker.notNull(capmMarket, "CAPM market");
    _name = name;
    _capmRiskFreeRate = capmRiskFreeRate;
    _capmMarket = capmMarket;
  }

  /**
   * Constructor to create a convention bundle for Bonds
   * @param name the descriptive name of the type
   * @param isEOMConvention whether the end-of-month convention should be followed when calculating date schedules
   * @param calculateScheduleFromMaturity whether to calculate the schedule from the maturity
   * @param exDividendDays the number of ex dividend days
   * @param settlementDays the number of days to settle
   * @param rollToSettlement whether to roll to settlement
   */
  public ConventionBundleImpl(final String name, final boolean isEOMConvention, final boolean calculateScheduleFromMaturity, final int exDividendDays,
      final int settlementDays, final boolean rollToSettlement) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.isTrue(exDividendDays >= 0, "ex-dividend days must be greater than zero");
    ArgumentChecker.isTrue(settlementDays >= 0, "settlement days must be greater than zero");
    _name = name;
    _isEOMConvention = isEOMConvention;
    _calculateScheduleFromMaturity = calculateScheduleFromMaturity;
    _exDividendDays = exDividendDays;
    _settlementDays = settlementDays;
    _rollToSettlement = rollToSettlement;
  }

  /**
   * Constructor to create a convention bundle for Bonds
   * @param name the descriptive name of the type
   * @param isEOMConvention whether the end-of-month convention should be followed when calculating date schedules
   * @param calculateScheduleFromMaturity whether to calculate the schedule from the maturity
   * @param exDividendDays the number of ex dividend days
   * @param shortSettlementDays the number of days to settle for short bonds
   * @param longSettlementDays the number of days to settle for long bonds
   * @param rollToSettlement whether to roll to settlement
   * @param cutoffTenor The cutoff tenor to decide whether a bond is long or short
   */
  public ConventionBundleImpl(final String name, final boolean isEOMConvention, final boolean calculateScheduleFromMaturity, final int exDividendDays,
      final int shortSettlementDays, final int longSettlementDays, final boolean rollToSettlement, final Tenor cutoffTenor) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.isTrue(exDividendDays >= 0, "ex-dividend days must be greater than zero");
    ArgumentChecker.isTrue(shortSettlementDays >= 0, "short settlement days must be greater than zero");
    ArgumentChecker.isTrue(longSettlementDays >= 0, "long settlement days must be greater than zero");
    _name = name;
    _isEOMConvention = isEOMConvention;
    _calculateScheduleFromMaturity = calculateScheduleFromMaturity;
    _exDividendDays = exDividendDays;
    _shortSettlementDays = shortSettlementDays;
    _longSettlementDays = longSettlementDays;
    _rollToSettlement = rollToSettlement;
    _cutoffTenor = cutoffTenor;
  }

  /**
   * Constructor to create a convention bundle for Bond Future deliverables
   * @param name the descriptive name of the type
   * @param isEOMConvention whether the end-of-month convention should be followed when calculating date schedules
   * @param calculateScheduleFromMaturity whether to calculate the schedule from the maturity
   * @param exDividendDays the number of ex dividend days
   * @param settlementDays the number of days to settle
   * @param dayCount the day count convention
   * @param businessDayConvention the business day convention
   * @param yieldConvention the yield convention
   */
  public ConventionBundleImpl(final String name, final boolean isEOMConvention, final boolean calculateScheduleFromMaturity, final int exDividendDays,
      final int settlementDays, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final YieldConvention yieldConvention) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.isTrue(exDividendDays >= 0, "ex-dividend days must be greater than zero");
    ArgumentChecker.isTrue(settlementDays >= 0, "settlement days must be greater than zero");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(businessDayConvention, "business day convention");
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

  /**
   * Constructor to create a convention bundle for Swaptions
   * @param name the descriptive name of the type
   * @param isCashSettled whether the Swaption is cash settled
   */
  public ConventionBundleImpl(final String name, final boolean isCashSettled) {
    ArgumentChecker.notNull(name, "name");
    _name = name;
    _isCashSettled = isCashSettled;
  }

  public ConventionBundleImpl(final ExternalIdBundle initialBundle, final String name, final String optionExpiryCalculator) {
    ArgumentChecker.notNull(initialBundle, "initial bundle");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(optionExpiryCalculator, "option expiry calculator");
    _bundle = initialBundle;
    _name = name;
    _optionExpiryCalculator = optionExpiryCalculator;
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
  public Integer getSettlementDays() {
    return _settlementDays;
  }

  // REVIEW: 2012-11-26 Andrew -- The getBondSettlementDays method puts behavior into what should be just a data object. I've exposed the cutofftenor
  // field so that this subclass of the bundle can be serialized. Any other subclasses will cause an error as it will not be possible to represent
  // their behavior.

  public Tenor getCutoffTenor() {
    return _cutoffTenor;
  }

  public int getShortSettlementDays() {
    return _shortSettlementDays;
  }

  public int getLongSettlementDays() {
    return _longSettlementDays;
  }

  @Override
  public Integer getBondSettlementDays(final ZonedDateTime bondSettlementDate, final ZonedDateTime bondMaturityDate) {
    if (_cutoffTenor != null) {
      if (bondSettlementDate.plus(_cutoffTenor.getPeriod()).isBefore(bondMaturityDate)) {
        return _shortSettlementDays;
      }
      return _longSettlementDays;
    }
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
   * {@inheritDoc}
   */
  @Override
  public Frequency getSwapFixedLegCompoundingFrequency() {
    return _swapFixedLegCompoundingFrequency;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InterestRate.Type getSwapFixedLegCompoundingType() {
    return _swapFixedLegCompoundingType;
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
    return _swapFloatingLegPaymentFrequency;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Frequency getSwapFloatingLegCompoundingFrequency() {
    return _swapFloatingLegCompoundingFrequency;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InterestRate.Type getSwapFloatingLegCompoundingType() {
    return _swapFloatingLegCompoundingType;
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

  @Override
  public String getOptionExpiryCalculator() {
    return _optionExpiryCalculator;
  }
}
