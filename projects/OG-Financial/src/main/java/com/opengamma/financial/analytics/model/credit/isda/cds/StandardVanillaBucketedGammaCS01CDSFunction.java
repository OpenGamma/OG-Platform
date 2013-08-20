/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cds;

import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla.isda.ISDACreditDefaultSwapBucketedGammaCS01Calculator;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * 
 */
public class StandardVanillaBucketedGammaCS01CDSFunction extends StandardVanillaCS01CDSFunction {
  private static final ISDACreditDefaultSwapBucketedGammaCS01Calculator CALCULATOR = new ISDACreditDefaultSwapBucketedGammaCS01Calculator();

  public StandardVanillaBucketedGammaCS01CDSFunction() {
    super(ValueRequirementNames.BUCKETED_GAMMA_CS01);
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
                                                ISDACompliantCreditCurve hazardCurve) {
    throw new UnsupportedOperationException();
    //final Double spreadCurveBump = Double.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_BUMP)));
    //final SpreadBumpType spreadBumpType = SpreadBumpType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_BUMP_TYPE)));
    //final PriceType priceType = PriceType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_CDS_PRICE_TYPE)));
    //final double[] gammaCS01 = CALCULATOR.getGammaCS01BucketedCreditDefaultSwap(valuationDate, definition, yieldCurve, times, marketSpreads, spreadCurveBump,
    //    spreadBumpType, priceType);
    //final int n = times.length;
    //final LocalDate[] dates = new LocalDate[n];
    //for (int i = 0; i < n; i++) {
    //  dates[i] = times[i].toLocalDate();
    //}
    //final LocalDateLabelledMatrix1D cs01Matrix = new LocalDateLabelledMatrix1D(dates, gammaCS01);
    //final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.BUCKETED_GAMMA_CS01, target.toSpecification(), properties);
    //return Collections.singleton(new ComputedValue(spec, cs01Matrix));
  }

}
