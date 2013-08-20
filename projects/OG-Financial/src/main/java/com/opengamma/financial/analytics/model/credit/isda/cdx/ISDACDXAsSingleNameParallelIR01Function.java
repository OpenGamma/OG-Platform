/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cdx;

import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla.isda.ISDACreditDefaultSwapParallelIR01Calculator;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * 
 */
public class ISDACDXAsSingleNameParallelIR01Function extends ISDACDXAsSingleNameIR01Function {
  private static final ISDACreditDefaultSwapParallelIR01Calculator CALCULATOR = new ISDACreditDefaultSwapParallelIR01Calculator();

  public ISDACDXAsSingleNameParallelIR01Function() {
    super(ValueRequirementNames.IR01);
  }

  @Override
  protected Set<ComputedValue> getComputedValue(final CreditDefaultSwapDefinition definition,
                                                final ISDACompliantYieldCurve yieldCurve,
                                                final ZonedDateTime[] times,
                                                final double[] marketSpreads,
                                                final ZonedDateTime valuationDate,
                                                final ComputationTarget target,
                                                final ValueProperties properties,
                                                final FunctionInputs inputs) {
    throw new UnsupportedOperationException();
    //final Double interestRateCurveBump = Double.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_INTEREST_RATE_CURVE_BUMP)));
    //final InterestRateBumpType interestRateBumpType =
    //    InterestRateBumpType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_INTEREST_RATE_BUMP_TYPE)));
    //final PriceType priceType = PriceType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_CDS_PRICE_TYPE)));
    //final double ir01 = CALCULATOR.getIR01ParallelShiftCreditDefaultSwap(valuationDate, definition, yieldCurve, times, marketSpreads, interestRateCurveBump,
    //    interestRateBumpType, priceType);
    //final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.IR01, target.toSpecification(), properties);
    //return Collections.singleton(new ComputedValue(spec, ir01));
  }

}
