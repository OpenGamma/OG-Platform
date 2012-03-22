/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.functiondoc;

import java.util.HashMap;
import java.util.Map;

/**
 * Temporary measure for formatting an internal security type string into a
 * form that can be displayed in the documentation. Note that the web gui
 * requires the same behaviour and will get a proper implementation. When that
 * happens, use that and delete this class.
 */
public final class PrettyPrintSecurityType {

  private static final Map<String, String> s_data;

  static {
    s_data = new HashMap<String, String>();
    s_data.put("BOND", "Bond");
    s_data.put("CAP-FLOOR", "Cap/Floor");
    s_data.put("CAP-FLOOR CMS SPREAD", "Cap/Floor CMS Spread");
    s_data.put("CASH", "Cash");
    s_data.put("EQUITY", "Equity");
    s_data.put("EQUITY VARIANCE SWAP", "Equity Variance Swap");
    s_data.put("EQUITY_BARRIER_OPTION", "Equity Barrier Option");
    s_data.put("EQUITY_INDEX_OPTION", "Equity Index Option");
    s_data.put("EQUITY_OPTION", "Equity Option");
    s_data.put("EXTERNAL_SENSITIVITIES_SECURITY", "Externally Calculated Sensitivities");
    s_data.put("FRA", "FRA");
    s_data.put("FUTURE", "Future");
    s_data.put("FX_BARRIER_OPTION", "FX Barrier Option");
    s_data.put("FX_DIGITAL_OPTION", "FX Digital Option");
    s_data.put("FX_FORWARD", "FX Forward");
    s_data.put("FX_OPTION", "FX Option");
    s_data.put("IRFUTURE_OPTION", "IR Future Option");
    s_data.put("SWAP", "Swap");
    s_data.put("SWAPTION", "Swaption");
  }

  private PrettyPrintSecurityType() {

  }

  public static String getTypeString(final String type) {
    final String value = s_data.get(type);
    if (value != null) {
      return value;
    }
    return type;
  }

}
