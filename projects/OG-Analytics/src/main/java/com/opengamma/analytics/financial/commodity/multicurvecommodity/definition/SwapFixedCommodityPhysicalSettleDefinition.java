/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.definition;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.annuity.AnnuityCouponCommodityPhysicalSettleDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * Class describing a physical settle commodity swap.
 */
public class SwapFixedCommodityPhysicalSettleDefinition extends SwapDefinition {

  /**
  * Constructor of the fixed-commodity physical settle swap from its two legs. This constructor is intended to be used when there is an initial floating
  * rate defined in the swap contract - the stream of payments on the floating leg then consists of a {@link CouponFixedDefinition} and
  * then a series of {@link CouponCommodityCashSettleDefinition}.
  * @param fixedLeg The fixed leg.
  * @param commodityLeg The commodity physical settle leg.
  */
  public SwapFixedCommodityPhysicalSettleDefinition(final AnnuityCouponFixedDefinition fixedLeg, final AnnuityDefinition<? extends PaymentDefinition> commodityLeg) {
    super(fixedLeg, commodityLeg);
    ArgumentChecker.isTrue(fixedLeg.getCurrency().equals(commodityLeg.getCurrency()), "legs should have the same currency");
  }

  /**
   * Constructor of the fixed-commodity physical settle swap from its two legs.
   * @param fixedLeg The fixed leg.
   * @param commodityLeg The commodity physical settle leg.
   */
  public SwapFixedCommodityPhysicalSettleDefinition(final AnnuityCouponFixedDefinition fixedLeg, final AnnuityCouponCommodityPhysicalSettleDefinition commodityLeg) {
    super(fixedLeg, commodityLeg);
    ArgumentChecker.isTrue(fixedLeg.getCurrency().equals(commodityLeg.getCurrency()), "legs should have the same currency");
  }

}
