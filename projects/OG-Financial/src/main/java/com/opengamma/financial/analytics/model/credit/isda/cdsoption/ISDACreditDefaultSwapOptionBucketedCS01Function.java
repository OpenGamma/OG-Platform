/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cdsoption;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.credit.bumpers.SpreadBumpType;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition.CreditDefaultSwapOptionDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.greeks.CS01CreditDefaultSwapOption;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.LocalDateLabelledMatrix1D;
import com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues;

/**
 * 
 */
public class ISDACreditDefaultSwapOptionBucketedCS01Function extends ISDACreditDefaultSwapOptionCS01Function {
  private static final CS01CreditDefaultSwapOption CALCULATOR = new CS01CreditDefaultSwapOption();
  public ISDACreditDefaultSwapOptionBucketedCS01Function() {
    super(ValueRequirementNames.BUCKETED_CS01);
  }

  @Override
  protected Set<ComputedValue> getComputedValue(final CreditDefaultSwapOptionDefinition definition, final ISDADateCurve yieldCurve, final double vol,
      final ZonedDateTime[] calibrationTenors, final double[] marketSpreads, final HazardRateCurve hazardRateCurve, final ZonedDateTime valuationTime,
      final ComputationTarget target, final ValueProperties properties) {
    final Double spreadCurveBump = Double.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_BUMP)));
    final SpreadBumpType spreadBumpType = SpreadBumpType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_BUMP_TYPE)));
    final double[] cs01 = CALCULATOR.getCS01BucketedCreditDefaultSwapOption(valuationTime, definition, vol, yieldCurve, hazardRateCurve, calibrationTenors, marketSpreads, spreadCurveBump,
        spreadBumpType);
    final int n = calibrationTenors.length;
    final LocalDate[] dates = new LocalDate[n];
    for (int i = 0; i < n; i++) {
      dates[i] = calibrationTenors[i].toLocalDate();
    }
    final LocalDateLabelledMatrix1D cs01Matrix = new LocalDateLabelledMatrix1D(dates, cs01);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.BUCKETED_CS01, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, cs01Matrix));
  }

}
