/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.functiondoc;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.forward.CommodityForwardSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;

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
    s_data.put(BondSecurity.SECURITY_TYPE, "Bond");
    s_data.put(BondFutureOptionSecurity.SECURITY_TYPE, "Bond Future Option");
    s_data.put(CapFloorSecurity.SECURITY_TYPE, "Cap/Floor");
    s_data.put(CapFloorCMSSpreadSecurity.SECURITY_TYPE, "Cap/Floor CMS Spread");
    s_data.put(CashSecurity.SECURITY_TYPE, "Cash");
    s_data.put(CommodityForwardSecurity.SECURITY_TYPE, "Commodity Forward");
    s_data.put(CommodityFutureOptionSecurity.SECURITY_TYPE, "Commodity Future Option");
    s_data.put(EquitySecurity.SECURITY_TYPE, "Equity");
    s_data.put(EquityBarrierOptionSecurity.SECURITY_TYPE, "Equity Barrier Option");
    s_data.put(EquityIndexOptionSecurity.SECURITY_TYPE, "Equity Index Option");
    s_data.put(EquityIndexDividendFutureOptionSecurity.SECURITY_TYPE, "Equity Index Future Option");
    s_data.put(EquityVarianceSwapSecurity.SECURITY_TYPE, "Equity Variance Swap");
    s_data.put(EquityOptionSecurity.SECURITY_TYPE, "Equity Option");
    s_data.put("EXTERNAL_SENSITIVITIES_SECURITY", "Externally Calculated Sensitivities");
    s_data.put(FRASecurity.SECURITY_TYPE, "FRA");
    s_data.put(FutureSecurity.SECURITY_TYPE, "Future");
    s_data.put(FXBarrierOptionSecurity.SECURITY_TYPE, "FX Barrier Option");
    s_data.put(FXDigitalOptionSecurity.SECURITY_TYPE, "FX Digital Option");
    s_data.put(FXForwardSecurity.SECURITY_TYPE, "FX Forward");
    s_data.put(FXOptionSecurity.SECURITY_TYPE, "FX Option");
    s_data.put(IRFutureOptionSecurity.SECURITY_TYPE, "IR Future Option");
    s_data.put(NonDeliverableFXDigitalOptionSecurity.SECURITY_TYPE, "Non-deliverable FX Digital Option");
    s_data.put(NonDeliverableFXOptionSecurity.SECURITY_TYPE, "Non-deliverable FX Option");
    s_data.put(NonDeliverableFXForwardSecurity.SECURITY_TYPE, "Non-deliverable FX Forward");
    s_data.put(PeriodicZeroDepositSecurity.SECURITY_TYPE, "Periodic Zero Deposit");
    s_data.put(SwapSecurity.SECURITY_TYPE, "Swap");
    s_data.put(SwaptionSecurity.SECURITY_TYPE, "Swaption");
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
