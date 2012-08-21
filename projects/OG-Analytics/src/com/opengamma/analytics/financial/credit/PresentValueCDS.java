/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;

/**
 * 
 */
public class PresentValueCDS {

  public PresentValueCDS() {

  }

  public double getPresentValueCDS(CreditDefaultSwapDefinition definition, YieldCurve curve) {
    double vFee = 0;
    double vCont = 0;

    double notional = definition.getNotional();
    //double notional = 10000000;

    double r = 0.0;
    double h = 0.0;

    double dcf = 0.25; //365.25 / (360 * 4);

    double delta = 0.4;
    double s = 1; //60.0 / 10000.0;

    int i = 0;
    int n = 20;

    for (i = 1; i <= n; i++) {

      double t =  i / 4;
      double z = curve.getDiscountFactor(t); //Math.exp(-r * t);

      vFee = vFee + notional * dcf * z * Math.exp(-h * t);
      vCont = vCont + notional * (1 - delta) * z * (Math.exp(-h * (t - 1) - Math.exp(-h * t)));
      System.out.println(vFee);
    }

    double v = -s * vFee + vCont;

    return v;
  }
}

