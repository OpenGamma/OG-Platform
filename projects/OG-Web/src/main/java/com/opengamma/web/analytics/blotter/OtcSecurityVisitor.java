/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * Visitor which returns true if the security is an OTC security that can be entered in the blotter in the web UI.
 */
/* package */ class OtcSecurityVisitor extends FinancialSecurityVisitorSameValueAdapter<Boolean> {

  /* package */ OtcSecurityVisitor() {
    super(false);
  }

  @Override
  public Boolean visitFXForwardSecurity(FXForwardSecurity security) {
    return true;
  }

  @Override
  public Boolean visitCapFloorCMSSpreadSecurity(CapFloorCMSSpreadSecurity security) {
    return true;
  }

  @Override
  public Boolean visitCapFloorSecurity(CapFloorSecurity security) {
    return true;
  }

  @Override
  public Boolean visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security) {
    return true;
  }

  @Override
  public Boolean visitSwaptionSecurity(SwaptionSecurity security) {
    return true;
  }

  @Override
  public Boolean visitSwapSecurity(SwapSecurity security) {
    return true;
  }

  @Override
  public Boolean visitNonDeliverableFXOptionSecurity(NonDeliverableFXOptionSecurity security) {
    return true;
  }

  @Override
  public Boolean visitFXOptionSecurity(FXOptionSecurity security) {
    return true;
  }

  @Override
  public Boolean visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security) {
    return true;
  }

  @Override
  public Boolean visitFRASecurity(FRASecurity security) {
    return true;
  }
}
