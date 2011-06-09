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

  public static String[] getCurveNamesForFundingCurveInstrument(StripInstrumentType type, String fundingCurveName,
      String forwardCurveName) {
    switch (type) {
      case SWAP:
        return new String[] {fundingCurveName, forwardCurveName};
      case CASH:
        return new String[] {fundingCurveName};
      case FRA:
        return new String[] {fundingCurveName, forwardCurveName};
      case FUTURE:
        return new String[] {fundingCurveName};
      case LIBOR:
        return new String[] {fundingCurveName};
      case TENOR_SWAP:
        return new String[] {fundingCurveName, forwardCurveName, fundingCurveName};
      default:
        throw new OpenGammaRuntimeException("Could not find " + type + " in funding curve instrument list");
    }
  }

  public static String[] getCurveNamesForForwardCurveInstrument(StripInstrumentType type, String fundingCurveName,
      String forwardCurveName) {
    switch (type) {
      case SWAP:
        return new String[] {fundingCurveName, forwardCurveName};
      case CASH:
        return new String[] {forwardCurveName};
      case FRA:
        return new String[] {fundingCurveName, forwardCurveName};
      case FUTURE:
        return new String[] {forwardCurveName};
      case LIBOR:
        return new String[] {forwardCurveName};
      case TENOR_SWAP:
        return new String[] {fundingCurveName, fundingCurveName, forwardCurveName};
      default:
        throw new OpenGammaRuntimeException("Could not find " + type + " in forward curve instrument list");
    }
  }

  public static String[] getCurveNamesForSecurity(FinancialSecurity security, String fundingCurveName,
      String forwardCurveName) {
    InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity(security);
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
        return new String[] {fundingCurveName};
      case COUPON_BOND:
        return new String[] {fundingCurveName};
      default:
        throw new OpenGammaRuntimeException("Could not find " + type + " in security instrument list");
    }
  }

  public static ValueProperties getValuePropertiesForSecurity(FinancialSecurity security, String fundingCurveName,
      String forwardCurveName, Builder properties) {
    String[] curveNames = getCurveNamesForSecurity(security, fundingCurveName, forwardCurveName);
    Currency ccy = FinancialSecurityUtils.getCurrency(security);
    properties.with(ValuePropertyNames.CURRENCY, ccy.getCode());    
    for (String name : curveNames) {
      if (name.equals(fundingCurveName)) {
        properties.with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName);
      }
      if (name.equals(forwardCurveName)) {
        properties.with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName);
      }
    }
    return properties.get();
  }

}
