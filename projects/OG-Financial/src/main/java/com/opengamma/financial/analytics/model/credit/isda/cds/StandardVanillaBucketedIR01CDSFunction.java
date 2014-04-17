/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cds;

import static com.opengamma.financial.analytics.model.credit.CreditFunctionUtils.getCoupon;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
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
import com.opengamma.financial.analytics.LocalDateLabelledMatrix1D;
import com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class StandardVanillaBucketedIR01CDSFunction extends StandardVanillaIR01CDSFunction {
  private static final InterestRateSensitivityCalculator CALCULATOR = new InterestRateSensitivityCalculator();

  public StandardVanillaBucketedIR01CDSFunction() {
    super(ValueRequirementNames.BUCKETED_IR01);
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

    final LocalDate[] dates = new LocalDate[yieldCurve.getNumberOfKnots()];

    final double[] ir01 = getBucketedIR01(definition, yieldCurve, valuationDate, properties, hazardCurve, analytic, dates);

    //final String[] labels = CreditFunctionUtils.getFormattedBucketedXAxis(dates, valuationDate);
    final LocalDateLabelledMatrix1D ir01Matrix = new LocalDateLabelledMatrix1D(dates, ir01);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.BUCKETED_IR01, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, ir01Matrix));
  }

  public static double[] getBucketedIR01(final CreditDefaultSwapDefinition definition,
                                   final ISDACompliantYieldCurve yieldCurve,
                                   final ZonedDateTime valuationDate,
                                   final ValueProperties properties,
                                   final ISDACompliantCreditCurve hazardCurve, final CDSAnalytic analytic, final LocalDate[] dates) {
    final Double interestRateCurveBump = Double.valueOf(Iterables.getOnlyElement(properties.getValues(
        CreditInstrumentPropertyNamesAndValues.PROPERTY_INTEREST_RATE_CURVE_BUMP)));
    //final InterestRateBumpType interestRateBumpType = InterestRateBumpType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_INTEREST_RATE_BUMP_TYPE)));
    //final PriceType priceType = PriceType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_CDS_PRICE_TYPE)));

    final double[] ir01 = CALCULATOR.bucketedIR01(analytic, getCoupon(definition), hazardCurve, yieldCurve);
    for (int i = 0; i < ir01.length; i++) {
      ir01[i] *= interestRateCurveBump * definition.getNotional();
      dates[i] = valuationDate.plusDays((long) yieldCurve.getTimeAtIndex(i) * 365).toLocalDate();
    }
    return ir01;
  }

}
