/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.CreditInstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
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
 * Definition of a Legacy Muni CDS i.e. with the features of CDS contracts prior to the Big Bang in 2009
 *@deprecated this will be deleted 
 */
@Deprecated
public class LegacyMuniCreditDefaultSwapDefinition extends LegacyCreditDefaultSwapDefinition {

  //----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Check hashCode and equals methods (fix these)

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables specific to the legacy Muni CDS contract

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Ctor for the Legacy Muni CDS

  public LegacyMuniCreditDefaultSwapDefinition(final BuySellProtection buySellProtection, final LegalEntity protectionBuyer, final LegalEntity protectionSeller, final LegalEntity referenceEntity,
      final Currency currency, final DebtSeniority debtSeniority, final RestructuringClause restructuringClause, final Calendar calendar, final ZonedDateTime startDate,
      final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final StubType stubType, final PeriodFrequency couponFrequency, final DayCount daycountFractionConvention,
      final BusinessDayConvention businessdayAdjustmentConvention, final boolean immAdjustMaturityDate, final boolean adjustEffectiveDate, final boolean adjustMaturityDate, final double notional,
      final double recoveryRate, final boolean includeAccruedPremium, final boolean protectionStart, final double parSpread) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Call the ctor for the LegacyCreditDefaultSwapDefinition superclass (corresponding to the CDS characteristics common to all types of CDS)

    super(buySellProtection, protectionBuyer, protectionSeller, referenceEntity, currency, debtSeniority, restructuringClause, calendar, startDate, effectiveDate, maturityDate, stubType,
        couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustEffectiveDate, adjustMaturityDate, notional, recoveryRate, includeAccruedPremium,
        protectionStart, parSpread);

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  @Override
  public LegacyMuniCreditDefaultSwapDefinition withStartDate(final ZonedDateTime startDate) {
    return new LegacyMuniCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(), getReferenceEntity(), getCurrency(), getDebtSeniority(),
        getRestructuringClause(), getCalendar(), startDate, getEffectiveDate(), getMaturityDate(), getStubType(), getCouponFrequency(), getDayCountFractionConvention(),
        getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(), getRecoveryRate(), getIncludeAccruedPremium(),
        getProtectionStart(), getParSpread());
  }

  @Override
  public LegacyMuniCreditDefaultSwapDefinition withEffectiveDate(final ZonedDateTime effectiveDate) {
    return new LegacyMuniCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(), getReferenceEntity(), getCurrency(), getDebtSeniority(),
        getRestructuringClause(), getCalendar(), getStartDate(), effectiveDate, getMaturityDate(), getStubType(), getCouponFrequency(), getDayCountFractionConvention(),
        getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(), getRecoveryRate(), getIncludeAccruedPremium(),
        getProtectionStart(), getParSpread());
  }

  @Override
  public LegacyMuniCreditDefaultSwapDefinition withSpread(final double parSpread) {
    return new LegacyMuniCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(), getReferenceEntity(), getCurrency(), getDebtSeniority(),
        getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), getMaturityDate(), getStubType(), getCouponFrequency(), getDayCountFractionConvention(),
        getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(), getRecoveryRate(), getIncludeAccruedPremium(),
        getProtectionStart(), parSpread);
  }

  @Override
  public LegacyMuniCreditDefaultSwapDefinition withCouponFrequency(final PeriodFrequency couponFrequency) {
    return new LegacyMuniCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(), getReferenceEntity(), getCurrency(), getDebtSeniority(),
        getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), getMaturityDate(), getStubType(), couponFrequency, getDayCountFractionConvention(),
        getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(), getRecoveryRate(), getIncludeAccruedPremium(),
        getProtectionStart(), getParSpread());
  }

  @Override
  public LegacyMuniCreditDefaultSwapDefinition withMaturityDate(final ZonedDateTime maturityDate) {
    return new LegacyMuniCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(), getReferenceEntity(), getCurrency(), getDebtSeniority(),
        getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), maturityDate, getStubType(), getCouponFrequency(), getDayCountFractionConvention(),
        getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(), getRecoveryRate(), getIncludeAccruedPremium(),
        getProtectionStart(), getParSpread());
  }

  @Override
  public CreditDefaultSwapDefinition withRecoveryRate(final double recoveryRate) {
    return new LegacyMuniCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(), getReferenceEntity(), getCurrency(), getDebtSeniority(),
        getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), getMaturityDate(), getStubType(), getCouponFrequency(), getDayCountFractionConvention(),
        getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(), recoveryRate, getIncludeAccruedPremium(),
        getProtectionStart(), getParSpread());
  }

  @Override
  public <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(final CreditInstrumentDefinitionVisitor<DATA_TYPE, RESULT_TYPE> visitor, final DATA_TYPE data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitLegacyMuniCDS(this, data);
  }

  @Override
  public <RESULT_TYPE> RESULT_TYPE accept(final CreditInstrumentDefinitionVisitor<Void, RESULT_TYPE> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitLegacyMuniCDS(this);
  }
}
