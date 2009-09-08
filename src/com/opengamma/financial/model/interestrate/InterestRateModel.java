package com.opengamma.financial.model.interestrate;

/**
 * 
 * @author emcleod
 * 
 */

public interface InterestRateModel<T> {

  public double getInterestRate(T x) throws Exception;

}
