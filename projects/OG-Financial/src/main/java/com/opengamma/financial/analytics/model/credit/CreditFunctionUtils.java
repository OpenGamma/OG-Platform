/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.curve.ConfigDBCurveSpecificationBuilder;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.credit.ConfigDBCurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class CreditFunctionUtils {

  @SuppressWarnings("rawtypes")
  public static Tenor[] getTenors(final Comparable[] xs) {
    final Tenor[] tenors = new Tenor[xs.length];
    for (int i = 0; i < xs.length; i++) {
      if (xs[i] instanceof Tenor) {
        tenors[i] = (Tenor) xs[i];
      } else {
        tenors[i] = new Tenor(Period.parse((String) xs[i]));
      }
    }
    return tenors;
  }

  public static Double[] getSpreads(final Object[] ys) {
    final Double[] spreads = new Double[ys.length];
    for (int i = 0; i < ys.length; i++) {
      spreads[i] = (Double) ys[i];
    }
    return spreads;
  }

  public static CurveSpecification getCurveSpecification(final ConfigSource configSource, final LocalDate curveDate, final String curveName) {
    final CurveDefinitionSource curveDefinitionSource = new ConfigDBCurveDefinitionSource(configSource);
    final CurveDefinition curveDefinition = curveDefinitionSource.getCurveDefinition(curveName);
    if (curveDefinition == null) {
      throw new OpenGammaRuntimeException("Could not get curve definition called " + curveName);
    }
    final CurveSpecificationBuilder curveSpecificationBuilder = new ConfigDBCurveSpecificationBuilder(configSource);
    return curveSpecificationBuilder.buildCurve(curveDate, curveDefinition);
  }
}
