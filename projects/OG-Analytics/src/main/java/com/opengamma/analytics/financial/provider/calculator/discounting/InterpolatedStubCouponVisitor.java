/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.DepositIndexCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.IborInterpolatedStubCompoundingCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.InterpolatedStubCoupon;

/**
 * Interface for interpolated stub coupon visitors.
 * 
 * @param <RESULT_TYPE>
 */
public interface InterpolatedStubCouponVisitor<RESULT_TYPE> {

  RESULT_TYPE visitIborCompoundingInterpolatedStub(IborInterpolatedStubCompoundingCoupon coupon);
  
  RESULT_TYPE visitIborInterpolatedStub(InterpolatedStubCoupon<DepositIndexCoupon<IborIndex>, IborIndex> coupon);
  
  RESULT_TYPE visitOvernightInterpolatedStub(InterpolatedStubCoupon<DepositIndexCoupon<IndexON>, IndexON> coupon);
  
}
