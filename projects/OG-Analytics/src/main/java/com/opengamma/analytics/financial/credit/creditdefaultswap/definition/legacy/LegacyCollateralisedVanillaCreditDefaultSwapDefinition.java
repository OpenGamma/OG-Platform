/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.collateralmodel.CreditSupportAnnexDefinition;
import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class LegacyCollateralisedVanillaCreditDefaultSwapDefinition extends LegacyVanillaCreditDefaultSwapDefinition {

  //----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Check hashCode and equals methods (fix these)
  // TODO : Remove the builder method code

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables specific to the legacy collateralised CDS contract

  // The CSA that this trade is executed under (specifies the details of the collateral arrangements between the protection buyer and seller)
  private final CreditSupportAnnexDefinition _creditSupportAnnex;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Ctor for the Legacy vanilla collateralised CDS

  public LegacyCollateralisedVanillaCreditDefaultSwapDefinition(
      final BuySellProtection buySellProtection,
      final Obligor protectionBuyer,
      final Obligor protectionSeller,
      final Obligor referenceEntity,
      final Currency currency,
      final DebtSeniority debtSeniority,
      final RestructuringClause restructuringClause,
      final Calendar calendar,
      final ZonedDateTime startDate,
      final ZonedDateTime effectiveDate,
      final ZonedDateTime maturityDate,
      final StubType stubType,
      final PeriodFrequency couponFrequency,
      final DayCount daycountFractionConvention,
      final BusinessDayConvention businessdayAdjustmentConvention,
      final boolean immAdjustMaturityDate,
      final boolean adjustEffectiveDate,
      final boolean adjustMaturityDate,
      final double notional,
      final double recoveryRate,
      final boolean includeAccruedPremium,
      final boolean protectionStart,
      final double parSpread,
      final CreditSupportAnnexDefinition creditSupportAnnex) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Call the ctor for the LegacyCreditDefaultSwapDefinition superclass (corresponding to the CDS characteristics common to all types of CDS)

    super(buySellProtection,
        protectionBuyer,
        protectionSeller,
        referenceEntity,
        currency,
        debtSeniority,
        restructuringClause,
        calendar,
        startDate,
        effectiveDate,
        maturityDate,
        stubType,
        couponFrequency,
        daycountFractionConvention,
        businessdayAdjustmentConvention,
        immAdjustMaturityDate,
        adjustEffectiveDate,
        adjustMaturityDate,
        notional,
        recoveryRate,
        includeAccruedPremium,
        protectionStart,
        parSpread);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    ArgumentChecker.notNull(creditSupportAnnex, "Credit support annex");

    _creditSupportAnnex = creditSupportAnnex;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  public CreditSupportAnnexDefinition getCreditSupportAnnexDefinition() {
    return _creditSupportAnnex;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
