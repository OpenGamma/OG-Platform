/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public final class FixedIncomeInstrumentCurveExposureHelper {

  private FixedIncomeInstrumentCurveExposureHelper() {
  }

  public static String[] getCurveNamesForFundingCurveInstrument(final StripInstrumentType type, final String fundingCurveName, final String forwardCurveName) {
    switch (type) {
      case SWAP_3M:
        return new String[] {fundingCurveName, forwardCurveName};
      case SWAP_6M:
        return new String[] {fundingCurveName, forwardCurveName};
      case CASH:
        return new String[] {fundingCurveName};
      case FRA_3M:
        return new String[] {fundingCurveName, forwardCurveName};
      case FRA_6M:
        return new String[] {fundingCurveName, forwardCurveName};
      case FUTURE:
        return new String[] {fundingCurveName, forwardCurveName};
      case LIBOR:
        return new String[] {fundingCurveName};
      case EURIBOR:
        return new String[] {fundingCurveName};
      case CDOR:
        return new String[] {fundingCurveName};
      case CIBOR:
        return new String[] {fundingCurveName};
      case STIBOR:
        return new String[] {fundingCurveName};
      case TENOR_SWAP:
        return new String[] {fundingCurveName, forwardCurveName, fundingCurveName};
      case OIS_SWAP:
        return new String[] {fundingCurveName, fundingCurveName};
      default:
        throw new OpenGammaRuntimeException("Could not find " + type + " in funding curve instrument list");
    }
  }

  public static String[] getCurveNamesForForwardCurveInstrument(final StripInstrumentType type, final String fundingCurveName, final String forwardCurveName) {
    switch (type) {
      case SWAP_3M:
        return new String[] {fundingCurveName, forwardCurveName};
      case SWAP_6M:
        return new String[] {fundingCurveName, forwardCurveName};
      case CASH:
        return new String[] {forwardCurveName};
      case FRA_3M:
        return new String[] {fundingCurveName, forwardCurveName};
      case FRA_6M:
        return new String[] {fundingCurveName, forwardCurveName};
      case FUTURE:
        return new String[] {fundingCurveName, forwardCurveName};
      case LIBOR:
        return new String[] {forwardCurveName};
      case EURIBOR:
        return new String[] {forwardCurveName};
      case CDOR:
        return new String[] {forwardCurveName};
      case CIBOR:
        return new String[] {forwardCurveName};
      case STIBOR:
        return new String[] {forwardCurveName};
      case TENOR_SWAP:
        return new String[] {fundingCurveName, fundingCurveName, forwardCurveName};
      case OIS_SWAP:
        return new String[] {fundingCurveName, fundingCurveName};
      default:
        throw new OpenGammaRuntimeException("Could not find " + type + " in forward curve instrument list");
    }
  }

  public static String[] getCurveNamesForSecurity(final FinancialSecurity security, final String fundingCurveName, final String forwardCurveName) {
    final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity(security);
    switch (type) {
      case SWAP_FIXED_IBOR:
        return new String[] {fundingCurveName, forwardCurveName};
      case SWAP_IBOR_IBOR:
        return new String[] {fundingCurveName, forwardCurveName, forwardCurveName};
      case CASH:
        return new String[] {fundingCurveName};
      case FRA:
        return new String[] {fundingCurveName, forwardCurveName};
      case IR_FUTURE:
        return new String[] {fundingCurveName, forwardCurveName};
      case COUPON_BOND:
        return new String[] {fundingCurveName, fundingCurveName};
      case SWAP_FIXED_CMS:
        return new String[] {fundingCurveName, forwardCurveName};
      case SWAP_IBOR_CMS:
        return new String[] {fundingCurveName, forwardCurveName};
      case SWAP_CMS_CMS:
        return new String[] {fundingCurveName, forwardCurveName};
      default:
        throw new OpenGammaRuntimeException("Could not find " + type + " in security instrument list");
    }
  }

  public static ValueProperties getValuePropertiesForSecurity(final FinancialSecurity security, final Builder properties) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(security);
    properties.with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode()).withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE).withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .with(ValuePropertyNames.CURRENCY, ccy.getCode());
    return properties.get();
  }

  public static ValueProperties getValuePropertiesForSecurity(final FinancialSecurity security, final String fundingCurveName, final String forwardCurveName, final Builder properties) {
    final String[] curveNames = getCurveNamesForSecurity(security, fundingCurveName, forwardCurveName);
    final Currency ccy = FinancialSecurityUtils.getCurrency(security);
    for (final String name : curveNames) {
      if (name.equals(fundingCurveName)) {
        properties.with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName);
      }
      if (name.equals(forwardCurveName)) {
        properties.with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName);
      }
    }
    properties.with(ValuePropertyNames.CURRENCY, ccy.getCode());
    properties.with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode());
    return properties.get();
  }

}
