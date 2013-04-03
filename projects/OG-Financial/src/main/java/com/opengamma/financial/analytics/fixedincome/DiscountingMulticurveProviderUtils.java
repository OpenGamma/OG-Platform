/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.financial.convention.CurveExposureConfigurationSource;
import com.opengamma.financial.convention.converter.IborIndexConverter;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class DiscountingMulticurveProviderUtils {
  private static final Logger s_logger = LoggerFactory.getLogger(DiscountingMulticurveProviderUtils.class);

  /**
   *
   */
  public DiscountingMulticurveProviderUtils() {
  }

  public static Map<Currency, YieldAndDiscountCurve> getDiscountingCurves(final FinancialSecurity security, final FunctionInputs inputs,
      final CurveExposureConfigurationSource configurationSource, final SecuritySource securitySource) {
    return null;
  }

  public static Map<IborIndex, YieldAndDiscountCurve> getForwardIborCurves(final FinancialSecurity security, final FunctionInputs inputs,
      final CurveExposureConfigurationSource configurationSource, final SecuritySource securitySource, final IborIndexConverter indexConverter) {
    return null;
  }

  public static Map<IndexON, YieldAndDiscountCurve> getForwardONCurves(final FinancialSecurity security) {
    return null;
  }

  public static FXMatrix getFXMatrix(final FinancialSecurity security) {
    return null;
  }

  public static MulticurveProviderDiscount getCurveProvider(final FinancialSecurity security) {
    final Map<Currency, YieldAndDiscountCurve> discountingCurves = new HashMap<>();
    final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves = new HashMap<>();
    final Map<IndexON, YieldAndDiscountCurve> forwardONCurves = new HashMap<>();
    final FXMatrix fxMatrix = new FXMatrix();
    return new MulticurveProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, fxMatrix);
  }

}
