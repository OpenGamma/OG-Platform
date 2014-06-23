/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.definition;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.TotalReturnSwap;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of a bill total return swap.
 */
public class BillTotalReturnSwap extends TotalReturnSwap {

  /** The underlying bill */
  private final BillSecurity _bill;
  /** The quantity of the bill referenced in the TRS. Can be negative or positive. */
  private final double _quantity;

  /**
   * Constructor of the total return swap.
   * @param effectiveTime The time to the effective date.
   * @param terminatioTime The time to the termination date.
   * @param fundingLeg The funding leg, not null
   * @param bill The bill security underlying the TRS. Not null.
   * @param billQuantity The quantity of the bond reference in the TRS. Can be negative or positive.
   */
  public BillTotalReturnSwap(final double effectiveTime, final double terminatioTime,
      final Annuity<? extends Payment> fundingLeg, final BillSecurity bill, final double billQuantity) {
    super(effectiveTime, terminatioTime, fundingLeg);
    ArgumentChecker.notNull(bill, "bond");
    _bill = bill;
    _quantity = billQuantity;
  }

  /**
   * Gets the bill underlying the TRS..
   * @return The bill.
   */
  public BillSecurity getAsset() {
    return _bill;
  }

  /**
   * Returns the bond quantity.
   * @return The quantity.
   */
  public double getQuantity() {
    return _quantity;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitBillTotalReturnSwap(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitBillTotalReturnSwap(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _bill.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_quantity);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BillTotalReturnSwap other = (BillTotalReturnSwap) obj;
    if (!ObjectUtils.equals(_bill, other._bill)) {
      return false;
    }
    if (Double.doubleToLongBits(_quantity) != Double.doubleToLongBits(other._quantity)) {
      return false;
    }
    return true;
  }

}
