/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Arrays;
import java.util.Set;

import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class YieldCurveLabelGenerator {

  public static Object[] getLabels(final InterpolatedYieldCurveSpecificationWithSecurities spec, final Currency currency, final String curveName) {
    final Set<FixedIncomeStripWithSecurity> strips = spec.getStrips();
    final int n = strips.size();
    final Object[] labels = new Object[n];
    int i = 0;
    for (final FixedIncomeStripWithSecurity strip : strips) {
      labels[i++] = strip.getTenor();
    }
    Arrays.sort(labels);
    return labels;
  }
  //
  //  public static Object[] getLabels(final InterpolatedYieldCurveDefinitionSource definitionSource, final Currency currency, final String curveName) {
  //    final YieldCurveDefinition definition = definitionSource.getDefinition(currency, curveName);
  //    if (definition == null) {
  //      throw new NullPointerException("Could not get definition for currency " + currency + ", name " + curveName);
  //    }
  //    final Set<FixedIncomeStrip> strips = definition.getStrips();
  //    final int n = strips.size();
  //    final Object[] labels = new Object[n];
  //    int i = 0;
  //    for (final FixedIncomeStrip strip : strips) {
  //      labels[i++] = strip.getCurveNodePointTime();
  //    }
  //    Arrays.sort(labels);
  //    return labels;
  //
  //  }
}
