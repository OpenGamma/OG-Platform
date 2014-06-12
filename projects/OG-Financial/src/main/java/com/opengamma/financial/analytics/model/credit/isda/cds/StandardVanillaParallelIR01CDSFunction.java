/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cds;

import static com.opengamma.financial.analytics.model.credit.CreditFunctionUtils.getCoupon;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.credit.bumpers.InterestRateBumpType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.InterestRateSensitivityCalculator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class StandardVanillaParallelIR01CDSFunction extends StandardVanillaIR01CDSFunction {
  private static final InterestRateSensitivityCalculator CALCULATOR = new InterestRateSensitivityCalculator();

  public StandardVanillaParallelIR01CDSFunction() {
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
                                                final FunctionInputs inputs,
                                                final ISDACompliantCreditCurve hazardCurve, final CDSAnalytic analytic, final Tenor[] tenors) {
    final double ir01 = getParallelIR01(definition, yieldCurve, properties, hazardCurve, analytic);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.IR01, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, ir01));
  }

  public static double getParallelIR01(final CreditDefaultSwapDefinition definition,
                                 final ISDACompliantYieldCurve yieldCurve,
                                 final ValueProperties properties,
                                 final ISDACompliantCreditCurve hazardCurve,
                                 final CDSAnalytic analytic) {
    final Double interestRateCurveBump = Double.valueOf(Iterables.getOnlyElement(properties.getValues(
        CreditInstrumentPropertyNamesAndValues.PROPERTY_INTEREST_RATE_CURVE_BUMP)));
    final InterestRateBumpType interestRateBumpType =
        InterestRateBumpType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_INTEREST_RATE_BUMP_TYPE)));
    //final PriceType priceType = PriceType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_CDS_PRICE_TYPE)));

    return interestRateCurveBump * definition.getNotional() * CALCULATOR.parallelIR01(analytic, getCoupon(definition), hazardCurve, yieldCurve);
  }

}
