/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cdx;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaParallelCS01CDSFunction;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class ISDACDXAsSingleNameParallelCS01Function extends ISDACDXAsSingleNameCS01Function {

  public ISDACDXAsSingleNameParallelCS01Function() {
    super(ValueRequirementNames.CS01);
  }

  @SuppressWarnings("deprecation")
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
                                                CDSAnalytic analytic,
                                                Tenor[] tenors) {

    //TODO: bump type
    Double bump = Double.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_BUMP)));
    
    CDSAnalyticFactory analyticFactory = new CDSAnalyticFactory(definition.getRecoveryRate(), definition.getCouponFrequency().getPeriod()).with(definition.getBusinessDayAdjustmentConvention())
        .with(definition.getCalendar()).with(definition.getStubType()).withAccrualDCC(definition.getDayCountFractionConvention());

    final CDSAnalytic pricingCDS = analyticFactory.makeCDS(valuationDate.toLocalDate(), definition.getEffectiveDate().toLocalDate(), definition.getMaturityDate().toLocalDate());
    
    double cs01 = StandardVanillaParallelCS01CDSFunction.parallelCS01(definition, yieldCurve, times, marketSpreads, pricingCDS, bump * 1e-4, definition.getBuySellProtection(), tenors);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.CS01, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, cs01));
  }

}
