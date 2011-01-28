/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Arrays;
import java.util.Set;

import com.opengamma.core.common.Currency;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;

/**
 * 
 */
public class YieldCurveLabelGenerator {

  public static Object[] getLabels(final InterpolatedYieldCurveDefinitionSource definitionSource, final Currency currency, final String curveName) {
    final YieldCurveDefinition definition = definitionSource.getDefinition(currency, curveName);
    if (definition == null) {
      throw new NullPointerException("Could not get definition for currency " + currency + ", name " + curveName);
    }
    final Set<FixedIncomeStrip> strips = definition.getStrips();
    final int n = strips.size();
    final Object[] labels = new Object[n];
    int i = 0;
    for (final FixedIncomeStrip strip : strips) {
      labels[i++] = strip.getCurveNodePointTime();
    }
    Arrays.sort(labels);
    return labels;
  }

}
