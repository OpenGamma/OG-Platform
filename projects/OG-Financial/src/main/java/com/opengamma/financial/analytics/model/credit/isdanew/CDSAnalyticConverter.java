/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isdanew;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;

//TODO: This should be a visitor but the credit definition is not in the instrument definition hierarchy

/**
 * Creates a {@link CDSAnalytic} object from the security
 * Throws an exception if the security cannot be converted.
 */
public class CDSAnalyticConverter {

  /**
   * Create a {@link CDSAnalytic} from a definition.
   * @param security the cds definition
   * @param valuationDate the valuation date
   * @return CDSAnalytic
   */
  public static CDSAnalytic create(final CreditDefaultSwapDefinition security, final LocalDate valuationDate) {
    return create(security, valuationDate, null);
  }

  /**
   * Create a {@link CDSAnalytic} from a definition.
   * @param security the cds definition
   * @param valuationDate the valuation date
   * @param maturityDate the maturity date - if null take from security
   * @return CDSAnalytic
   */
  public static CDSAnalytic create(final CreditDefaultSwapDefinition security, final LocalDate valuationDate, final LocalDate maturityDate) {
    final CDSAnalytic cdsAnalytic = new CDSAnalytic(valuationDate,
                                                    security.getEffectiveDate().toLocalDate(),
                                                    valuationDate.plusDays(3), //FIXME: Hard code or get from somewhere else?
                                                    security.getStartDate().toLocalDate(),
                                                    maturityDate != null ? maturityDate : security.getMaturityDate().toLocalDate(),
                                                    true, //FIXME: Do we have this info anywhere?
                                                    security.getCouponFrequency().getPeriod(),
                                                    security.getStubType(),
                                                    security.getProtectionStart(),
                                                    security.getRecoveryRate(),
                                                    security.getBusinessDayAdjustmentConvention() ,
                                                    security.getCalendar(),
                                                    security.getDayCountFractionConvention()
    );
    return cdsAnalytic;
  }

}
