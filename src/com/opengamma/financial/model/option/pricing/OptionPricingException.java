package com.opengamma.financial.model.option.pricing;

/**
 * 
 * @author emcleod
 */
public class OptionPricingException extends Exception {

  public OptionPricingException() {
    super();
  }

  public OptionPricingException(String s) {
    super(s);
  }

  public OptionPricingException(String s, Throwable cause) {
    super(s, cause);
  }

  public OptionPricingException(Throwable cause) {
    super(cause);
  }
}
