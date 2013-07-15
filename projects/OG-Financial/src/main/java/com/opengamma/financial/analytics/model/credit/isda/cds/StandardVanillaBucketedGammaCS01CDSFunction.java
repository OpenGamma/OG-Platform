/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cds;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.bumpers.SpreadBumpType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla.isda.ISDACreditDefaultSwapBucketedGammaCS01Calculator;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.LocalDateLabelledMatrix1D;
import com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues;

/**
 * 
 */
public class StandardVanillaBucketedGammaCS01CDSFunction extends StandardVanillaCS01CDSFunction {
  private static final ISDACreditDefaultSwapBucketedGammaCS01Calculator CALCULATOR = new ISDACreditDefaultSwapBucketedGammaCS01Calculator();

  public StandardVanillaBucketedGammaCS01CDSFunction() {
    super(ValueRequirementNames.BUCKETED_GAMMA_CS01);
  }

  @Override
  protected Set<ComputedValue> getComputedValue(final CreditDefaultSwapDefinition definition, final ISDADateCurve yieldCurve, final ZonedDateTime[] times,
      final double[] marketSpreads, final ZonedDateTime valuationDate, final ComputationTarget target, final ValueProperties properties,
      final FunctionInputs inputs) {
    final Double spreadCurveBump = Double.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_BUMP)));
    final SpreadBumpType spreadBumpType = SpreadBumpType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_BUMP_TYPE)));
    final PriceType priceType = PriceType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_CDS_PRICE_TYPE)));
    final double[] gammaCS01 = CALCULATOR.getGammaCS01BucketedCreditDefaultSwap(valuationDate, definition, yieldCurve, times, marketSpreads, spreadCurveBump,
        spreadBumpType, priceType);
    final int n = times.length;
    final LocalDate[] dates = new LocalDate[n];
    for (int i = 0; i < n; i++) {
      dates[i] = times[i].toLocalDate();
    }
    final LocalDateLabelledMatrix1D cs01Matrix = new LocalDateLabelledMatrix1D(dates, gammaCS01);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.BUCKETED_GAMMA_CS01, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, cs01Matrix));
  }

}
