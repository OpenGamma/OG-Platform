/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.CreditInstrumentDefinitionVisitor;
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
 * 
 *@deprecated this will be deleted 
 */
@Deprecated
public class StandardForwardStartingCreditDefaultSwapDefinition extends StandardCreditDefaultSwapDefinition {

  //----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to add the test file for this object
  // TODO : Check hashCode (and need to fix this) and equals methods
  // TODO : Need to add the member variables specific to this contract

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables specific to the standard forward starting CDS contract

  private final ZonedDateTime _forwardStartDate;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Ctor for the Standard fixed recovery CDS contract

  public StandardForwardStartingCreditDefaultSwapDefinition(final BuySellProtection buySellProtection, final LegalEntity protectionBuyer, final LegalEntity protectionSeller, final LegalEntity referenceEntity,
      final Currency currency, final DebtSeniority debtSeniority, final RestructuringClause restructuringClause, final Calendar calendar, final ZonedDateTime startDate,
      final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final StubType stubType, final PeriodFrequency couponFrequency, final DayCount daycountFractionConvention,
      final BusinessDayConvention businessdayAdjustmentConvention, final boolean immAdjustMaturityDate, final boolean adjustEffectiveDate, final boolean adjustMaturityDate, final double notional,
      final double recoveryRate, final boolean includeAccruedPremium, final boolean protectionStart, final double quotedSpread, final double premiumLegCoupon, final double upfrontAmount,
      final ZonedDateTime cashSettlementDate, final boolean adjustCashSettlementDate, final ZonedDateTime forwardStartDate) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Call the ctor for the superclass (corresponding to the CDS characteristics common to all types of CDS)

    super(buySellProtection, protectionBuyer, protectionSeller, referenceEntity, currency, debtSeniority, restructuringClause, calendar, startDate, effectiveDate, maturityDate, stubType,
        couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustEffectiveDate, adjustMaturityDate, notional, recoveryRate, includeAccruedPremium,
        protectionStart, quotedSpread, premiumLegCoupon, upfrontAmount, cashSettlementDate, adjustCashSettlementDate);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    ArgumentChecker.notNull(forwardStartDate, "Forward start date");

    _forwardStartDate = forwardStartDate;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  public ZonedDateTime getForwardStartDate() {
    return _forwardStartDate;
  }

  @Override
  public StandardForwardStartingCreditDefaultSwapDefinition withSpread(final double spread) {
    return new StandardForwardStartingCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(), getReferenceEntity(), getCurrency(), getDebtSeniority(),
        getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), getMaturityDate(), getStubType(), getCouponFrequency(), getDayCountFractionConvention(),
        getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(), getRecoveryRate(), getIncludeAccruedPremium(),
        getProtectionStart(), spread, getPremiumLegCoupon(), getUpfrontAmount(), getCashSettlementDate(), getAdjustCashSettlementDate(), _forwardStartDate);
  }

  @Override
  public StandardForwardStartingCreditDefaultSwapDefinition withStartDate(final ZonedDateTime startDate) {
    return new StandardForwardStartingCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(), getReferenceEntity(), getCurrency(), getDebtSeniority(),
        getRestructuringClause(), getCalendar(), startDate, getEffectiveDate(), getMaturityDate(), getStubType(), getCouponFrequency(), getDayCountFractionConvention(),
        getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(), getRecoveryRate(), getIncludeAccruedPremium(),
        getProtectionStart(), getQuotedSpread(), getPremiumLegCoupon(), getUpfrontAmount(), getCashSettlementDate(), getAdjustCashSettlementDate(), _forwardStartDate);
  }

  @Override
  public StandardForwardStartingCreditDefaultSwapDefinition withMaturityDate(final ZonedDateTime maturityDate) {
    return new StandardForwardStartingCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(), getReferenceEntity(), getCurrency(), getDebtSeniority(),
        getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), maturityDate, getStubType(), getCouponFrequency(), getDayCountFractionConvention(),
        getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(), getRecoveryRate(), getIncludeAccruedPremium(),
        getProtectionStart(), getQuotedSpread(), getPremiumLegCoupon(), getUpfrontAmount(), getCashSettlementDate(), getAdjustCashSettlementDate(), _forwardStartDate);
  }

  @Override
  public StandardForwardStartingCreditDefaultSwapDefinition withEffectiveDate(final ZonedDateTime effectiveDate) {
    return new StandardForwardStartingCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(), getReferenceEntity(), getCurrency(), getDebtSeniority(),
        getRestructuringClause(), getCalendar(), getStartDate(), effectiveDate, getMaturityDate(), getStubType(), getCouponFrequency(), getDayCountFractionConvention(),
        getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(), getRecoveryRate(), getIncludeAccruedPremium(),
        getProtectionStart(), getQuotedSpread(), getPremiumLegCoupon(), getUpfrontAmount(), getCashSettlementDate(), getAdjustCashSettlementDate(), _forwardStartDate);
  }

  @Override
  public StandardForwardStartingCreditDefaultSwapDefinition withRecoveryRate(final double recoveryRate) {
    return new StandardForwardStartingCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(), getReferenceEntity(), getCurrency(), getDebtSeniority(),
        getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), getMaturityDate(), getStubType(), getCouponFrequency(), getDayCountFractionConvention(),
        getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(), recoveryRate, getIncludeAccruedPremium(),
        getProtectionStart(), getQuotedSpread(), getPremiumLegCoupon(), getUpfrontAmount(), getCashSettlementDate(), getAdjustCashSettlementDate(), _forwardStartDate);
  }

  @Override
  public <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(final CreditInstrumentDefinitionVisitor<DATA_TYPE, RESULT_TYPE> visitor, final DATA_TYPE data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitStandardForwardStartingCDS(this, data);
  }

  @Override
  public <RESULT_TYPE> RESULT_TYPE accept(final CreditInstrumentDefinitionVisitor<Void, RESULT_TYPE> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitStandardForwardStartingCDS(this);
  }

}
