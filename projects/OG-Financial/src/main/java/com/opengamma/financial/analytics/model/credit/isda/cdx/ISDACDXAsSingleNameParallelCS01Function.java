/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cdx;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalytic;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.QuotedSpread;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.SpreadSensitivityCalculator;
import com.opengamma.analytics.financial.model.BumpType;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class ISDACDXAsSingleNameParallelCS01Function extends ISDACDXAsSingleNameCS01Function {
  private static final SpreadSensitivityCalculator CALCULATOR = new SpreadSensitivityCalculator();

  public ISDACDXAsSingleNameParallelCS01Function() {
    super(ValueRequirementNames.CS01);
  }

  @Override
  protected Set<ComputedValue> getComputedValue(final CreditDefaultSwapDefinition definition,
                                                final ISDACompliantYieldCurve yieldCurve,
                                                final ZonedDateTime[] times,
                                                final double[] marketSpreads,
                                                final ZonedDateTime valuationDate,
                                                final ComputationTarget target,
                                                final ValueProperties properties,
                                                final FunctionInputs inputs,
                                                ISDACompliantCreditCurve hazardCurve,
                                                CDSAnalytic analytic) {

    final CDSAnalyticFactory analyticFactory = new CDSAnalyticFactory(definition.getRecoveryRate(), definition.getCouponFrequency().getPeriod())
        .with(definition.getBusinessDayAdjustmentConvention())
        .with(definition.getCalendar()).with(definition.getStubType())
        .withAccualDCC(definition.getDayCountFractionConvention());
    final CDSAnalytic pricingCDS = analyticFactory.makeCDS(definition.getStartDate().toLocalDate(), definition.getEffectiveDate().toLocalDate(), definition.getMaturityDate().toLocalDate());

    Period[] tenors = new Period[times.length];
    for (int i = 0; i < times.length; i++) {
      tenors[i] = Period.between(definition.getStartDate().toLocalDate(), times[i].toLocalDate()).withDays(0);
    }
    CDSAnalytic[] pillars = analyticFactory.makeIMMCDS(definition.getStartDate().toLocalDate(), tenors);

    double cs01;
    if (definition instanceof StandardCreditDefaultSwapDefinition) {
      StandardCreditDefaultSwapDefinition cds = (StandardCreditDefaultSwapDefinition) definition;
      cs01 = CALCULATOR.parallelCS01(pricingCDS, new QuotedSpread(cds.getQuotedSpread(), 0.01), yieldCurve, 1e-4);
    } else if (definition instanceof LegacyCreditDefaultSwapDefinition) {
      cs01 = CALCULATOR.parallelCS01FromParSpreads(pricingCDS,
                                                   ((LegacyCreditDefaultSwapDefinition) definition).getParSpread(),
                                                   yieldCurve,
                                                   pillars,
                                                   marketSpreads,
                                                   1e-4,
                                                   BumpType.ADDITIVE);
    } else {
      throw new OpenGammaRuntimeException("Unexpected cds type: " + definition.getClass().getSimpleName());
    }

    //final Double spreadCurveBump = Double.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_BUMP)));
    //final SpreadBumpType spreadBumpType = SpreadBumpType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_BUMP_TYPE)));
    //final PriceType priceType = PriceType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_CDS_PRICE_TYPE)));
    //final double cs01 = CALCULATOR.getCS01ParallelShiftCreditDefaultSwap(valuationDate, definition, yieldCurve, times, marketSpreads, spreadCurveBump,
    //    spreadBumpType, priceType);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.CS01, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, cs01));
  }

}
