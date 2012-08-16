/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Test the simple CDS pricining method
 */
public class CDSSimpleMethodTest extends CDSTestSetup {

  @Test
  public void testPresetValue() {
    
    final ZonedDateTime pricingDate = ZonedDateTime.of(2010, 12, 31, 0, 0, 0, 0, TimeZone.UTC);
    
    final CDSDerivative cds = loadCDS_SimpleModel().toDerivative(pricingDate, "CDS_CCY", "SPREAD", "BOND_CCY");
    final YieldCurveBundle curveBundle = loadCurveBundle_SimpleModel();
    
    final CDSSimpleMethod method = new CDSSimpleMethod();
    final CurrencyAmount result = method.presentValue(cds, curveBundle);

    Assert.assertEquals(result.getAmount(), 0.06281112880507082);
  }
  
}
