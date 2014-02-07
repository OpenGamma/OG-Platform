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
import com.opengamma.analytics.financial.credit.centralcounterparty.CentralCounterpartyDefinition;
import com.opengamma.analytics.financial.credit.collateralmodel.CreditSupportAnnexDefinition;
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
public class StandardCollateralizedVanillaCreditDefaultSwapDefinition extends StandardVanillaCreditDefaultSwapDefinition {

  //----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to add the test file for this object
  // TODO : Need to add the hashCode and equals methods

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables specific to the standard collateralised CDS contract

  // The CSA that this trade is executed under (specifies the details of the collateral arrangements between the protection buyer and seller)
  private final CreditSupportAnnexDefinition _creditSupportAnnex;

  // The CCP that this trade is cleared through
  private final CentralCounterpartyDefinition _centralCounterparty;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Ctor for the Standard CDS contract

  public StandardCollateralizedVanillaCreditDefaultSwapDefinition(final BuySellProtection buySellProtection, final LegalEntity protectionBuyer, final LegalEntity protectionSeller,
      final LegalEntity referenceEntity, final Currency currency, final DebtSeniority debtSeniority, final RestructuringClause restructuringClause, final Calendar calendar, final ZonedDateTime startDate,
      final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final StubType stubType, final PeriodFrequency couponFrequency, final DayCount daycountFractionConvention,
      final BusinessDayConvention businessdayAdjustmentConvention, final boolean immAdjustMaturityDate, final boolean adjustEffectiveDate, final boolean adjustMaturityDate, final double notional,
      final double recoveryRate, final boolean includeAccruedPremium, final boolean protectionStart, final double quotedSpread, final double premiumLegCoupon, final double upfrontAmount,
      final ZonedDateTime cashSettlementDate, final boolean adjustCashSettlementDate, final CreditSupportAnnexDefinition creditSupportAnnex, final CentralCounterpartyDefinition centralCounterparty) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Call the ctor for the StandardVanillaCreditDefaultSwapDefinition superclass (corresponding to the CDS characteristics common to all types of CDS)

    super(buySellProtection, protectionBuyer, protectionSeller, referenceEntity, currency, debtSeniority, restructuringClause, calendar, startDate, effectiveDate, maturityDate, stubType,
        couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustEffectiveDate, adjustMaturityDate, notional, recoveryRate, includeAccruedPremium,
        protectionStart, quotedSpread, premiumLegCoupon, upfrontAmount, cashSettlementDate, adjustCashSettlementDate);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    ArgumentChecker.notNull(creditSupportAnnex, "Credit support annex");
    ArgumentChecker.notNull(centralCounterparty, "Central Counterparty");

    _creditSupportAnnex = creditSupportAnnex;

    _centralCounterparty = centralCounterparty;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public CreditSupportAnnexDefinition getCreditSupportAnnex() {
    return _creditSupportAnnex;
  }

  public CentralCounterpartyDefinition getCentralCounterparty() {
    return _centralCounterparty;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Override
  public StandardCollateralizedVanillaCreditDefaultSwapDefinition withSpread(final double spread) {
    return new StandardCollateralizedVanillaCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(), getReferenceEntity(), getCurrency(), getDebtSeniority(),
        getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), getMaturityDate(), getStubType(), getCouponFrequency(), getDayCountFractionConvention(),
        getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(), getRecoveryRate(), getIncludeAccruedPremium(),
        getProtectionStart(), spread, getPremiumLegCoupon(), getUpfrontAmount(), getCashSettlementDate(), getAdjustCashSettlementDate(), _creditSupportAnnex, _centralCounterparty);
  }

  @Override
  public StandardCollateralizedVanillaCreditDefaultSwapDefinition withMaturityDate(final ZonedDateTime maturityDate) {
    return new StandardCollateralizedVanillaCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(), getReferenceEntity(), getCurrency(), getDebtSeniority(),
        getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), maturityDate, getStubType(), getCouponFrequency(), getDayCountFractionConvention(),
        getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(), getRecoveryRate(), getIncludeAccruedPremium(),
        getProtectionStart(), getQuotedSpread(), getPremiumLegCoupon(), getUpfrontAmount(), getCashSettlementDate(), getAdjustCashSettlementDate(), _creditSupportAnnex, _centralCounterparty);
  }

  @Override
  public StandardCollateralizedVanillaCreditDefaultSwapDefinition withEffectiveDate(final ZonedDateTime effectiveDate) {
    return new StandardCollateralizedVanillaCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(), getReferenceEntity(), getCurrency(), getDebtSeniority(),
        getRestructuringClause(), getCalendar(), getStartDate(), effectiveDate, getMaturityDate(), getStubType(), getCouponFrequency(), getDayCountFractionConvention(),
        getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(), getRecoveryRate(), getIncludeAccruedPremium(),
        getProtectionStart(), getQuotedSpread(), getPremiumLegCoupon(), getUpfrontAmount(), getCashSettlementDate(), getAdjustCashSettlementDate(), _creditSupportAnnex, _centralCounterparty);
  }

  @Override
  public StandardCollateralizedVanillaCreditDefaultSwapDefinition withRecoveryRate(final double recoveryRate) {
    return new StandardCollateralizedVanillaCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(), getReferenceEntity(), getCurrency(), getDebtSeniority(),
        getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), getMaturityDate(), getStubType(), getCouponFrequency(), getDayCountFractionConvention(),
        getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(), recoveryRate, getIncludeAccruedPremium(),
        getProtectionStart(), getQuotedSpread(), getPremiumLegCoupon(), getUpfrontAmount(), getCashSettlementDate(), getAdjustCashSettlementDate(), _creditSupportAnnex, _centralCounterparty);
  }

  @Override
  public <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(final CreditInstrumentDefinitionVisitor<DATA_TYPE, RESULT_TYPE> visitor, final DATA_TYPE data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitStandardCollateralizedVanillaCDS(this, data);
  }

  @Override
  public <RESULT_TYPE> RESULT_TYPE accept(final CreditInstrumentDefinitionVisitor<Void, RESULT_TYPE> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitStandardCollateralizedVanillaCDS(this);
  }

}
