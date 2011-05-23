/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;

/**
 * 
 */
public class FixedIncomeInstrumentCurveExposureHelper {
  private final Map<StripInstrumentType, String[]> _fundingCurveInstruments;
  private final Map<StripInstrumentType, String[]> _forwardCurveInstruments;
  private final Map<FixedIncomeInstrumentType, String[]> _securityExposures;
  private final String _fundingCurveName;
  private final String _forwardCurveName;

  public FixedIncomeInstrumentCurveExposureHelper(String fundingCurveName, String forwardCurveName) {
    _fundingCurveName = fundingCurveName;
    _forwardCurveName = forwardCurveName;
    _fundingCurveInstruments = new HashMap<StripInstrumentType, String[]>();
    _fundingCurveInstruments.put(StripInstrumentType.SWAP, );
    _fundingCurveInstruments.put(StripInstrumentType.CASH, new String[] {fundingCurveName});
    _fundingCurveInstruments.put(StripInstrumentType.FRA, new String[] {fundingCurveName,
        forwardCurveName});
    _fundingCurveInstruments.put(StripInstrumentType.FUTURE, new String[] {fundingCurveName});
    _fundingCurveInstruments.put(StripInstrumentType.LIBOR, new String[] {fundingCurveName});
    _fundingCurveInstruments.put(StripInstrumentType.TENOR_SWAP, new String[] {fundingCurveName,
        forwardCurveName, fundingCurveName});
    _forwardCurveInstruments = new HashMap<StripInstrumentType, String[]>();
    _forwardCurveInstruments.put(StripInstrumentType.SWAP, new String[] {fundingCurveName,
        forwardCurveName});
    _forwardCurveInstruments.put(StripInstrumentType.CASH, new String[] {forwardCurveName});
    _forwardCurveInstruments.put(StripInstrumentType.FRA, new String[] {fundingCurveName,
        forwardCurveName});
    _forwardCurveInstruments.put(StripInstrumentType.FUTURE, new String[] {forwardCurveName});
    _forwardCurveInstruments.put(StripInstrumentType.LIBOR, new String[] {forwardCurveName});
    _forwardCurveInstruments.put(StripInstrumentType.TENOR_SWAP, new String[] {fundingCurveName,
        fundingCurveName, forwardCurveName});
    _securityExposures = new HashMap<FixedIncomeInstrumentType, String[]>();
    //TODO check all of these
    _securityExposures.put(FixedIncomeInstrumentType.CASH, new String[] {fundingCurveName});
    _securityExposures.put(FixedIncomeInstrumentType.FRA, new String[] {fundingCurveName, forwardCurveName});
    _securityExposures.put(FixedIncomeInstrumentType.IR_FUTURE, new String[] {forwardCurveName});
    _securityExposures.put(FixedIncomeInstrumentType.SWAP_FIXED_IBOR, new String[] {fundingCurveName, forwardCurveName});
    _securityExposures.put(FixedIncomeInstrumentType.SWAP_IBOR_IBOR, new String[] {fundingCurveName, forwardCurveName,
        forwardCurveName});
    _securityExposures.put(FixedIncomeInstrumentType.COUPON_BOND, new String[] {fundingCurveName});
  }

  public static String[] getCurveNamesForFundingCurveInstrument(StripInstrumentType type, String fundingCurveName, String forwardCurveName) {
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

  public static String[] getCurveNamesForForwardCurveInstrument(StripInstrumentType type, String fundingCurveName, String forwardCurveName) {
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

  public static String[] getCurveNamesForSecurity(Security security, String fundingCurveName, String forwardCurveName) {

  }

  public String[] getExposuresForSecurity(Security security) {
    String[] curveNames = _securityExposures.get(FixedIncomeInstrumentType.getInstrumentTypeFromSecurity(security));
    if (curveNames == null) {
      throw new OpenGammaRuntimeException("Could not get exposure information for this security type");
    }
    return curveNames;
  }

  public ValueProperties getValuePropertiesForSecurity(Security security) {
    return ValueProperties.with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(security).getCode())
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, _forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, _fundingCurveName).get();
  }
}
