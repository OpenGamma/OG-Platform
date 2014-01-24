package com.opengamma.analytics.financial.instrument.payment;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IndexDeposit;

public interface DepositIndexCouponDefinition<I extends IndexDeposit> {

  ZonedDateTime getAccrualStartDate();
  
  ZonedDateTime getAccrualEndDate();
  
  double getPaymentYearFraction();
  
  double getNotional();
}
