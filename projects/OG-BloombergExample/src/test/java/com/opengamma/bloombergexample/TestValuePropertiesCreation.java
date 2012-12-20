package com.opengamma.bloombergexample;

import org.testng.annotations.Test;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;

public class TestValuePropertiesCreation {

  @Test
  public void test() {
    final ValueProperties fundingProp = ValueProperties.with(ValuePropertyNames.CURVE_CURRENCY, "GBP").with(ValuePropertyNames.CURVE, "FUNDING").get();
    System.err.println(fundingProp);
    
    ValueProperties gbp = fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, "EUR").withOptional(ValuePropertyNames.CURVE_CURRENCY).get();
    System.err.println(gbp);
  }
}
