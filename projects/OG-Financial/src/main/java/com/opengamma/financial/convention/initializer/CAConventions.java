/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.initializer;


import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.DEPOSIT_ON;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.FIXED_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.IRS_IBOR_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.ON_CMP_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.OVERNIGHT;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.PAY_LAG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.QUARTERLY;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.SCHEME_NAME;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.STIR_FUTURES;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.TENOR_STR_1Y;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.TENOR_STR_3M;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.TENOR_STR_6M;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getConventionName;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getIds;

import org.threeten.bp.LocalTime;

import com.opengamma.analytics.financial.interestrate.CompoundingType;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.financial.convention.expirycalc.IMMFutureAndFutureOptionQuarterlyExpiryCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * The conventions for Canada.
 */
public class CAConventions extends ConventionMasterInitializer {

  /** Singleton. */
  public static final ConventionMasterInitializer INSTANCE = new CAConventions();
  /** The CDOR string **/
  public static final String CDOR = "CDOR";
  /** The CDOR leg string **/
  private static final String CDOR_CMP_LEG = CDOR + " Comp Leg";

  private static final BusinessDayConvention MODIFIED_FOLLOWING = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
  private static final DayCount ACT_365 = DayCounts.ACT_365; 
  private static final ExternalId CA = ExternalSchemes.financialRegionId("CA");
  private static final Currency CCY = Currency.CAD;

  /**
   * Restricted constructor.
   */
  protected CAConventions() {
  }

  //-------------------------------------------------------------------------
  @Override
  public void init(final ConventionMaster master) {
    
    // Index Overnight
    final String onIndexName = getConventionName(CCY, OVERNIGHT);
    final ExternalId onIndexId = ExternalId.of(SCHEME_NAME, onIndexName);
    final OvernightIndexConvention onIndex = createOvernightIndexConvention(onIndexName);
    
    // Index CDOR
    final String cdorConventionName = getConventionName(CCY, CDOR);
    final ExternalId cdorConventionId = ExternalId.of(SCHEME_NAME, cdorConventionName);
    final IborIndexConvention cdorIndex = createIborIndexConvention(cdorConventionName);
    
    // Deposit
    final String depositONConventionName = getConventionName(CCY, DEPOSIT_ON);
    final DepositConvention depositONConvention = createONDepositConvention(depositONConventionName);
    
    // Fixed Legs
    final String fixedLeg6MConventionName = getConventionName(CCY, TENOR_STR_6M, FIXED_LEG);
    final SwapFixedLegConvention fixedLeg6MConvention = createSwapFixedLegConvention(fixedLeg6MConventionName, TENOR_STR_6M, Tenor.SIX_MONTHS);
    final String fixedLeg1YConventionName = getConventionName(CCY, TENOR_STR_1Y, FIXED_LEG);
    final SwapFixedLegConvention fixedLeg1YConvention = createSwapFixedLegConvention(fixedLeg1YConventionName, TENOR_STR_1Y, Tenor.ONE_YEAR);
    final String fixedLeg1YPayLagConventionName = getConventionName(CCY, TENOR_STR_1Y, PAY_LAG + FIXED_LEG);
    final SwapFixedLegConvention fixedLeg1YPayLagConvention = createSwapFixedLegPayLagConvention(fixedLeg1YPayLagConventionName, TENOR_STR_1Y, Tenor.ONE_YEAR);
    
    // CDOR Legs
    final String cdor3M6MLegConventionName = getConventionName(CCY, TENOR_STR_3M + TENOR_STR_6M, CDOR_CMP_LEG);
    final CompoundingIborLegConvention cdor3M6MLegConvention = createCompoundingIborLegConvention(cdor3M6MLegConventionName, cdorConventionId, TENOR_STR_3M, Tenor.THREE_MONTHS,
        TENOR_STR_6M, Tenor.SIX_MONTHS);
    
    // Overnight Legs
    final String onLegConventionName = getConventionName(CCY, TENOR_STR_1Y, ON_CMP_LEG);
    final OISLegConvention onLegConvention = createOISLegConvention(onLegConventionName, onIndexId, TENOR_STR_1Y, Tenor.ONE_YEAR);
    
    // Futures
    final String quarterlySTIRFutureConventionName = getConventionName(CCY, STIR_FUTURES + QUARTERLY);    
    final InterestRateFutureConvention quarterlySTIRFutureConvention = new InterestRateFutureConvention(
        quarterlySTIRFutureConventionName, 
        ExternalIdBundle.of(ExternalId.of(SCHEME_NAME, quarterlySTIRFutureConventionName)),
        ExternalId.of(ExchangeTradedInstrumentExpiryCalculator.SCHEME, IMMFutureAndFutureOptionQuarterlyExpiryCalculator.NAME), CA, cdorConventionId);
    
    // TODO: Remove - Note: Temporally used to retrieve underlying index convention.
    
    final String irsibor3MLegConventionName = getConventionName(CCY, TENOR_STR_3M, IRS_IBOR_LEG);
    final VanillaIborLegConvention irsIbor3MLegConvention = new VanillaIborLegConvention(
        irsibor3MLegConventionName, getIds(CCY, TENOR_STR_3M, IRS_IBOR_LEG),
        cdorConventionId, true, Interpolator1DFactory.LINEAR, Tenor.THREE_MONTHS, 0, true, StubType.SHORT_START, false, 0);
    
    // Convention add
    addConvention(master, onIndex);
    addConvention(master, cdorIndex);
    addConvention(master, depositONConvention);
    addConvention(master, fixedLeg6MConvention);
    addConvention(master, fixedLeg1YConvention);
    addConvention(master, fixedLeg1YPayLagConvention);
    addConvention(master, cdor3M6MLegConvention);
    addConvention(master, onLegConvention);
    addConvention(master, quarterlySTIRFutureConvention);
    addConvention(master, irsIbor3MLegConvention);
  }

  protected OvernightIndexConvention createOvernightIndexConvention(final String onIndexName) {
    return new OvernightIndexConvention(
        onIndexName, getIds(CCY, OVERNIGHT), ACT_365, 0, CCY, CA);
  }

  protected IborIndexConvention createIborIndexConvention(final String bbswConventionName) {
    return new IborIndexConvention(
        bbswConventionName, getIds(CCY, CDOR), ACT_365, MODIFIED_FOLLOWING, 0, true, CCY,
        LocalTime.of(10, 00), "CA", CA, CA, "");
  }

  protected DepositConvention createONDepositConvention(final String depositONConventionName) {
    return new DepositConvention(
        depositONConventionName, getIds(CCY, DEPOSIT_ON), ACT_365, FOLLOWING, 0, false, CCY, CA);
  }

  protected SwapFixedLegConvention createSwapFixedLegConvention(final String fixedLegConventionName, 
      final String tenorString, final Tenor resetTenor) {
    return new SwapFixedLegConvention(
        fixedLegConventionName, getIds(CCY, tenorString, FIXED_LEG),
        resetTenor, ACT_365, MODIFIED_FOLLOWING, CCY, CA, 1, true, StubType.SHORT_START, false, 0);
  }

  protected SwapFixedLegConvention createSwapFixedLegPayLagConvention(final String fixedLeg1YPayLagConventionName, 
      final String tenorString, final Tenor resetTenor) {
    return new SwapFixedLegConvention(
        fixedLeg1YPayLagConventionName, getIds(CCY, tenorString, PAY_LAG + FIXED_LEG),
        resetTenor, ACT_365, MODIFIED_FOLLOWING, CCY, CA, 1, true, StubType.SHORT_START, false, 1);
  }

  protected CompoundingIborLegConvention createCompoundingIborLegConvention(final String cdorLegConventionName, final ExternalId cdorConventionId, 
      final String resetTenorString, final Tenor resetTenor, final String paymentTenorString,  final Tenor paymentTenor) {
    return new CompoundingIborLegConvention(cdorLegConventionName, getIds(CCY, resetTenorString + paymentTenorString, CDOR_CMP_LEG), cdorConventionId, 
        paymentTenor, CompoundingType.FLAT_COMPOUNDING, resetTenor, StubType.SHORT_START, 0, true, StubType.SHORT_START, false, 0);
  }

  protected OISLegConvention createOISLegConvention(final String onLegConventionName, final ExternalId onIndexId, 
      final String tenorString, final Tenor resetTenor) {
    return new OISLegConvention(
        onLegConventionName, getIds(CCY, tenorString, ON_CMP_LEG), onIndexId,
        resetTenor, MODIFIED_FOLLOWING, 0, true, StubType.SHORT_START, false, 0);
  }

}
