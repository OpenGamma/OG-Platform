/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class AnalyticHazardCurveBuild {

  public ISDACompliantCurve build(final CDSAnalytic[] cds, final ISDACompliantCurve yieldCurve) {
    final int n = cds.length;
    final double proStart = cds[0].getProtectionStart();
    for (int i = 1; i < n; i++) {
      ArgumentChecker.isTrue(proStart == cds[i].getProtectionStart(), "all cds must have same protection start");
      ArgumentChecker.isTrue(cds[i].getProtectionEnd() > cds[i - 1].getProtectionEnd(), "protection end must be ascending");
    }
    return null;
  }

}
