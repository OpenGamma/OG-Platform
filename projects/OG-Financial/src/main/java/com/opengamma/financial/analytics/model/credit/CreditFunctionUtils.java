/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.curve.ConfigDBCurveSpecificationBuilder;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.credit.ConfigDBCurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.id.VersionCorrection;
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

  public static CurveSpecification getCurveSpecification(final Instant valuationTime, final ConfigSource configSource, final LocalDate curveDate, final String curveName) {
    final Instant versionTime = valuationTime.plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);
    final CurveDefinitionSource curveDefinitionSource = new ConfigDBCurveDefinitionSource(configSource);
    final CurveDefinition curveDefinition = curveDefinitionSource.getCurveDefinition(curveName, VersionCorrection.of(versionTime, versionTime));
    if (curveDefinition == null) {
      throw new OpenGammaRuntimeException("Could not get curve definition called " + curveName);
    }
    final CurveSpecificationBuilder curveSpecificationBuilder = new ConfigDBCurveSpecificationBuilder(configSource);
    return curveSpecificationBuilder.buildCurve(valuationTime, curveDate, curveDefinition);
  }

  public static String[] getFormattedBucketedXAxis(final LocalDate[] dates, final ZonedDateTime valuationDateTime) {
    final LocalDate valuationDate = IMMDateGenerator.getPreviousIMMDate(valuationDateTime.toLocalDate());
    final int n = dates.length;
    final String[] result = new String[n];
    for (int i = 0; i < n; i++) {
      final Period periodBetween = Period.between(valuationDate, dates[i]);
      result[i] = periodBetween.toString();
    }
    return result;
  }
}
