/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.CreditInstrumentDefinition;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.isdastandardmodel.StubType;
import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *  Definition of a generic Single Name Credit Default Swap contract (abstract class therefore different types of CDS will inherit from this)
 *@deprecated this will be deleted 
 */
@Deprecated
public abstract class CreditDefaultSwapDefinition implements CreditInstrumentDefinition {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Cashflow Conventions are assumed to be as below (these will apply throughout the entire credit suite for credit default swaps)

  // Notional amount > 0 always - long/short positions are captured by the setting of the 'BuySellProtection' flag
  // This convention is chosen to avoid confusion about whether a negative notional means a long/short position etc

  // Buy protection -> Pay premium leg, receive contingent leg -> 'long' protection -> 'short' credit risk
  // Sell protection -> Receive premium leg, pay contingent leg -> 'short' protection -> 'long' credit risk

  // Coupon conventions - coupons are always assumed to be entered in bps (therefore there are internal conversions to absolute values by division by 10,000)

  // NOTE : For a standard CDS contract the step-in date is the same as the effective date

  // NOTE : We are enforcing the condition that the three obligors have to be different entities

  // NOTE : There are no builder methods implemented in this class because it is abstract

  // NOTE : If protectionStart = TRUE then default protection begins at the time 00:01 of startDate
  // NOTE : If protectionStart = FALSE then default protection begins at the time 23:59 of startDate

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : More detailed description of ref entity obligation will be necessary
  // TODO : Move _protectionStart and _protectionOffset variables into the PV calculator?
  // TODO : Replace rec rate range arg checkers with .isInRangeInclusive
  // TODO : Need to extract out the recovery rate input from this definition (since it is actually market data)

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables (all private and final) of the CDS contract (defines what a CDS is)

  // From the users perspective, are we buying or selling protection
  private final BuySellProtection _buySellProtection;

  // The (three) counterparties in the trade
  private final LegalEntity _protectionBuyer;
  private final LegalEntity _protectionSeller;
  private final LegalEntity _referenceEntity;

  // The currency the trade is executed in e.g. USD
  private final Currency _currency;

  // The seniority of the debt of the reference entity the CDS is written on
  private final DebtSeniority _debtSeniority;

  // The restructuring type in the event of a credit event deemed to be a restructuring of the reference entities debt
  private final RestructuringClause _restructuringClause;

  // Holiday calendar for the determination of adjusted business days in the cashflow schedule
  private final Calendar _calendar;

  // The date of the contract inception (the date at which the CDS is executed - not the valuation date)
  private final ZonedDateTime _startDate;

  // The effective date for protection to begin (usually T + 1d for a legacy CDS, T - 60d or T - 90d for a standard CDS where T is the valuation date)
  private final ZonedDateTime _effectiveDate;

  // The maturity date of the contract (when premium and protection coverage ceases)
  private final ZonedDateTime _maturityDate;

  // The type of stub (front/back and long/short)
  private final StubType _stubType;

  // The frequency of coupon payments (usually quarterly or semi-annual for legacy CDS and quarterly for standard CDS)
  private final PeriodFrequency _couponFrequency;

  // Day-count convention (usually Act/360 for legacy and standard CDS)
  private final DayCount _daycountFractionConvention;

  // Business day adjustment convention (usually following for legacy and standard CDS)
  private final BusinessDayConvention _businessdayAdjustmentConvention;

  // Flag to determine if we adjust the maturity date to fall on the next IMM date - not currently hooked up
  private final boolean _immAdjustMaturityDate;

  // Flag to determine if we business day adjust the user input effective date (not a feature of legacy or standard CDS) - not currently hooked up
  private final boolean _adjustEffectiveDate;

  // Flag to determine if we business day adjust the final maturity date (not a feature of legacy or standard CDS) - not currently hooked up
  private final boolean _adjustMaturityDate;

  // The trade notional (in the trade currency), convention is that this will always be a positive amount
  private final double _notional;

  // The recovery rate to be used in the calculation of the CDS MtM (the recovery used in pricing can be different to the rate used to calibrate the hazard rates)
  private final double _recoveryRate;

  // Flag to determine whether the accrued coupons should be included in the CDS premium leg calculation
  private final boolean _includeAccruedPremium;

  // Flag to determine if survival probabilities are calculated at the beginning or end of the day (hard coded to TRUE in ISDA model)
  private final boolean _protectionStart;

  // If _protectionStart = true then this is the offset (one extra day of protection)
  private final double _protectionOffset = 1.0 / 365.0;

  // The credit key to uniquely identify a reference entities par spread CDS curve (from a 4-tuple of Ticker-Currency-Restructuring-Seniority)
  private final String _creditKey;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Constructor for a CDS contract object

  /**
   * Create a CDS object 
   * @param buySellProtection Are we buying or selling the credit protection
   * @param protectionBuyer protection buyer
   * @param protectionSeller protection seller
   * @param referenceEntity reference entity
   * @param currency Currency 
   * @param debtSeniority Debt Seniority
   * @param restructuringClause Restructuring Clause
   * @param calendar Calendar
   * @param startDate Date when protection begins (this can be the start or the end of the day depending on the value of protectionStart)
   * @param effectiveDate Date when party assumes ownership (aka stepin date or assignment date). Currently must have  startDate <= effectiveDate which
   * means you cannot buy forward protection - TODO investigate whether this can be removed
   * @param maturityDate Date when protection ends (end of day)
   * @param stubType stub type
   * @param couponFrequency coupon frequency
   * @param daycountFractionConvention day-count convention
   * @param businessdayAdjustmentConvention business-day adjustment convention
   * @param immAdjustMaturityDate if true adjust IMM maturity date - TODO check exactly what this does 
   * @param adjustEffectiveDate if true adjust effective date for non-business days 
   * @param adjustMaturityDate if true adjust (non-IMM) maturity date for non-business days 
   * @param notional the notional 
   * @param recoveryRate the recovery rate (between 0 and 1.0)
   * @param includeAccruedPremium If true accrued premium must be paid in the event of default
   * @param protectionStart if true the protection is from the start of day 
   */
  public CreditDefaultSwapDefinition(final BuySellProtection buySellProtection, final LegalEntity protectionBuyer, final LegalEntity protectionSeller, final LegalEntity referenceEntity, final Currency currency,
      final DebtSeniority debtSeniority, final RestructuringClause restructuringClause, final Calendar calendar, final ZonedDateTime startDate, final ZonedDateTime effectiveDate,
      final ZonedDateTime maturityDate, final StubType stubType, final PeriodFrequency couponFrequency, final DayCount daycountFractionConvention,
      final BusinessDayConvention businessdayAdjustmentConvention, final boolean immAdjustMaturityDate, final boolean adjustEffectiveDate, final boolean adjustMaturityDate, final double notional,
      final double recoveryRate, final boolean includeAccruedPremium, final boolean protectionStart) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check the validity of the input arguments

    ArgumentChecker.notNull(buySellProtection, "Buy/Sell");

    ArgumentChecker.notNull(protectionBuyer, "Protection buyer");
    ArgumentChecker.notNull(protectionSeller, "Protection seller");
    ArgumentChecker.notNull(referenceEntity, "Reference entity");

    // Check that the counterparties in the contract are not equal to one another e.g. protectionSeller != protectionBuyer
    ArgumentChecker.isTrue(!protectionBuyer.equals(protectionSeller), "Protection buyer is identical to protection seller");
    ArgumentChecker.isTrue(!protectionBuyer.equals(referenceEntity), "Protection buyer is identical to reference entity");
    ArgumentChecker.isTrue(!protectionSeller.equals(referenceEntity), "Protection seller is identical to reference entity");

    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(debtSeniority, "Debt seniority");
    ArgumentChecker.notNull(restructuringClause, "Restructuring clause");

    ArgumentChecker.notNull(calendar, "Calendar");

    ArgumentChecker.notNull(startDate, "Start date");
    ArgumentChecker.notNull(effectiveDate, "Effective date");
    ArgumentChecker.notNull(maturityDate, "Maturity date");

    // Check the temporal ordering of the input dates (these are the unadjusted dates entered by the user)
    ArgumentChecker.isTrue(!startDate.isAfter(effectiveDate), "Start date {} must be on or before effective date {}", startDate, effectiveDate);
    ArgumentChecker.isTrue(!startDate.isAfter(maturityDate), "Start date {} must be on or before maturity date {}", startDate, maturityDate);
    // ArgumentChecker.isTrue(!effectiveDate.isAfter(maturityDate), "Effective date {} must be on or before maturity date {}", effectiveDate, maturityDate);

    ArgumentChecker.notNull(stubType, "Stub Type");
    ArgumentChecker.notNull(couponFrequency, "Coupon frequency");
    ArgumentChecker.notNull(daycountFractionConvention, "Daycount convention");
    ArgumentChecker.notNull(businessdayAdjustmentConvention, "Business day adjustment convention");

    ArgumentChecker.notNegative(notional, "Notional amount");

    ArgumentChecker.notNegative(recoveryRate, "Recovery Rate");
    ArgumentChecker.isTrue(recoveryRate <= 1.0, "Recovery rate should be less than or equal to 100%");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Assign the member variables for the CDS object

    _buySellProtection = buySellProtection;

    _protectionBuyer = protectionBuyer;
    _protectionSeller = protectionSeller;
    _referenceEntity = referenceEntity;

    _currency = currency;
    _debtSeniority = debtSeniority;
    _restructuringClause = restructuringClause;

    _calendar = calendar;

    _startDate = startDate;
    _effectiveDate = effectiveDate;
    _maturityDate = maturityDate;

    _stubType = stubType;
    _couponFrequency = couponFrequency;
    _daycountFractionConvention = daycountFractionConvention;
    _businessdayAdjustmentConvention = businessdayAdjustmentConvention;

    _immAdjustMaturityDate = immAdjustMaturityDate;
    _adjustEffectiveDate = adjustEffectiveDate;
    _adjustMaturityDate = adjustMaturityDate;

    _notional = notional;
    _recoveryRate = recoveryRate;

    _includeAccruedPremium = includeAccruedPremium;

    _protectionStart = protectionStart;

    // REVIEW 29/8/2012 think about using UniqueId instead of _creditKey
    _creditKey = _referenceEntity.getTicker() + "_" + _currency + "_" + _debtSeniority + "_" + _restructuringClause;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Abstract builder methods (have to be implemented in classes which derive from this one)

  public abstract CreditDefaultSwapDefinition withStartDate(ZonedDateTime startDate);

  public abstract CreditDefaultSwapDefinition withMaturityDate(ZonedDateTime maturityDate);

  public abstract CreditDefaultSwapDefinition withRecoveryRate(double recoveryRate);

  public abstract CreditDefaultSwapDefinition withEffectiveDate(ZonedDateTime effectiveDate);

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Public member accessor methods

  public BuySellProtection getBuySellProtection() {
    return _buySellProtection;
  }

  public LegalEntity getProtectionBuyer() {
    return _protectionBuyer;
  }

  public LegalEntity getProtectionSeller() {
    return _protectionSeller;
  }

  public LegalEntity getReferenceEntity() {
    return _referenceEntity;
  }

  public Currency getCurrency() {
    return _currency;
  }

  public DebtSeniority getDebtSeniority() {
    return _debtSeniority;
  }

  public RestructuringClause getRestructuringClause() {
    return _restructuringClause;
  }

  public Calendar getCalendar() {
    return _calendar;
  }

  public ZonedDateTime getStartDate() {
    return _startDate;
  }

  public ZonedDateTime getEffectiveDate() {
    return _effectiveDate;
  }

  public ZonedDateTime getMaturityDate() {
    return _maturityDate;
  }

  public StubType getStubType() {
    return _stubType;
  }

  public PeriodFrequency getCouponFrequency() {
    return _couponFrequency;
  }

  public DayCount getDayCountFractionConvention() {
    return _daycountFractionConvention;
  }

  public BusinessDayConvention getBusinessDayAdjustmentConvention() {
    return _businessdayAdjustmentConvention;
  }

  public boolean getIMMAdjustMaturityDate() {
    return _immAdjustMaturityDate;
  }

  public boolean getAdjustEffectiveDate() {
    return _adjustEffectiveDate;
  }

  public boolean getAdjustMaturityDate() {
    return _adjustMaturityDate;
  }

  public double getNotional() {
    return _notional;
  }

  public double getRecoveryRate() {
    return _recoveryRate;
  }

  public boolean getIncludeAccruedPremium() {
    return _includeAccruedPremium;
  }

  public boolean getProtectionStart() {
    return _protectionStart;
  }

  public String getCreditKey() {
    return _creditKey;
  }

  public double getProtectionOffset() {
    return _protectionOffset;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_adjustEffectiveDate ? 1231 : 1237);
    result = prime * result + (_adjustMaturityDate ? 1231 : 1237);
    result = prime * result + _businessdayAdjustmentConvention.hashCode();
    result = prime * result + _buySellProtection.hashCode();
    result = prime * result + _calendar.hashCode();
    result = prime * result + _couponFrequency.hashCode();
    result = prime * result + _creditKey.hashCode();
    result = prime * result + _currency.hashCode();
    result = prime * result + _daycountFractionConvention.hashCode();
    result = prime * result + _debtSeniority.hashCode();
    result = prime * result + _effectiveDate.hashCode();
    result = prime * result + (_immAdjustMaturityDate ? 1231 : 1237);
    result = prime * result + (_includeAccruedPremium ? 1231 : 1237);
    result = prime * result + _maturityDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _protectionBuyer.hashCode();
    temp = Double.doubleToLongBits(_protectionOffset);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _protectionSeller.hashCode();
    result = prime * result + (_protectionStart ? 1231 : 1237);
    temp = Double.doubleToLongBits(_recoveryRate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _referenceEntity.hashCode();
    result = prime * result + _restructuringClause.hashCode();
    result = prime * result + _startDate.hashCode();
    result = prime * result + _stubType.hashCode();
    return result;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CreditDefaultSwapDefinition)) {
      return false;
    }
    final CreditDefaultSwapDefinition other = (CreditDefaultSwapDefinition) obj;
    if (!ObjectUtils.equals(_creditKey, other._creditKey)) {
      return false;
    }
    if (Double.compare(_notional, other._notional) != 0) {
      return false;
    }
    if (!ObjectUtils.equals(_maturityDate, other._maturityDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_startDate, other._startDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_protectionBuyer, other._protectionBuyer)) {
      return false;
    }
    if (!ObjectUtils.equals(_protectionSeller, other._protectionSeller)) {
      return false;
    }
    if (!ObjectUtils.equals(_referenceEntity, other._referenceEntity)) {
      return false;
    }
    if (Double.compare(_protectionOffset, other._protectionOffset) != 0) {
      return false;
    }
    if (_protectionStart != other._protectionStart) {
      return false;
    }
    if (_buySellProtection != other._buySellProtection) {
      return false;
    }
    if (Double.compare(_recoveryRate, other._recoveryRate) != 0) {
      return false;
    }
    if (ObjectUtils.equals(_effectiveDate, other._effectiveDate)) {
      return false;
    }
    if (_adjustEffectiveDate != other._adjustEffectiveDate) {
      return false;
    }
    if (_adjustMaturityDate != other._adjustMaturityDate) {
      return false;
    }
    if (!ObjectUtils.equals(_couponFrequency, other._couponFrequency)) {
      return false;
    }
    if (!ObjectUtils.equals(_businessdayAdjustmentConvention, other._businessdayAdjustmentConvention)) {
      return false;
    }
    if (!ObjectUtils.equals(_daycountFractionConvention, other._daycountFractionConvention)) {
      return false;
    }
    if (_debtSeniority != other._debtSeniority) {
      return false;
    }
    if (_immAdjustMaturityDate != other._immAdjustMaturityDate) {
      return false;
    }
    if (_includeAccruedPremium != other._includeAccruedPremium) {
      return false;
    }
    if (_restructuringClause != other._restructuringClause) {
      return false;
    }
    if (_stubType != other._stubType) {
      return false;
    }
    if (!ObjectUtils.equals(_calendar, other._calendar)) {
      return false;
    }
    return true;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Override
  public String toString() {
    return "CreditDefaultSwapDefinition{" + "_buySellProtection=" + _buySellProtection + ", _protectionBuyer=" + _protectionBuyer + ", _protectionSeller=" + _protectionSeller + ", _referenceEntity=" +
        _referenceEntity + ", _currency=" + _currency + ", _debtSeniority=" + _debtSeniority + ", _restructuringClause=" + _restructuringClause + ", _calendar=" + _calendar + ", _startDate=" +
        _startDate + ", _effectiveDate=" + _effectiveDate + ", _maturityDate=" + _maturityDate + ", _stubType=" + _stubType + ", _couponFrequency=" + _couponFrequency +
        ", _daycountFractionConvention=" + _daycountFractionConvention + ", _businessdayAdjustmentConvention=" + _businessdayAdjustmentConvention + ", _immAdjustMaturityDate=" +
        _immAdjustMaturityDate + ", _adjustEffectiveDate=" + _adjustEffectiveDate + ", _adjustMaturityDate=" + _adjustMaturityDate + ", _notional=" + _notional + ", _recoveryRate=" + _recoveryRate +
        ", _includeAccruedPremium=" + _includeAccruedPremium + ", _protectionStart=" + _protectionStart + ", _creditKey='" + _creditKey + '\'' + ", _protectionOffset=" + _protectionOffset + '}';
  }
}
