/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db.option;

import com.opengamma.financial.security.db.EnumUserType;
import com.opengamma.financial.security.option.AssetOrNothingPayoffStyle;
import com.opengamma.financial.security.option.AsymmetricPoweredPayoffStyle;
import com.opengamma.financial.security.option.BarrierPayoffStyle;
import com.opengamma.financial.security.option.CappedPoweredPayoffStyle;
import com.opengamma.financial.security.option.CashOrNothingPayoffStyle;
import com.opengamma.financial.security.option.FixedStrikePayoffStyle;
import com.opengamma.financial.security.option.PayoffStyleVisitor;
import com.opengamma.financial.security.option.PoweredPayoffStyle;
import com.opengamma.financial.security.option.VanillaPayoffStyle;

/**
 * Custom Hibernate usertype for the OptionPayoffStyle enum
 */
public class OptionPayoffStyleUserType extends EnumUserType<OptionPayoffStyle> {

  private static final String ASSET_OR_NOTHING = "Asset-or-Nothing";
  private static final String ASYMMETRIC_POWERED = "Asymmetric Powered";
  private static final String BARRIER = "Barrier";
  private static final String CAPPED_POWERED = "Capped Powered";
  private static final String CASH_OR_NOTHING = "Cash-or-Nothing";
  private static final String FIXED_STRIKE = "Fixed Strike";
  private static final String POWERED = "Powered";
  private static final String VANILLA = "Vanilla";

  public OptionPayoffStyleUserType() {
    super(OptionPayoffStyle.class, OptionPayoffStyle.values());
  }

  @Override
  protected String enumToStringNoCache(OptionPayoffStyle value) {
    return value.accept(new PayoffStyleVisitor<String>() {

      @Override
      public String visitAssetOrNothingPayoffStyle(AssetOrNothingPayoffStyle payoffStyle) {
        return ASSET_OR_NOTHING;
      }

      @Override
      public String visitAsymmetricPoweredPayoffStyle(AsymmetricPoweredPayoffStyle payoffStyle) {
        return ASYMMETRIC_POWERED;
      }

      @Override
      public String visitBarrierPayoffStyle(BarrierPayoffStyle payoffStyle) {
        return BARRIER;
      }

      @Override
      public String visitCappedPoweredPayoffStyle(CappedPoweredPayoffStyle payoffStyle) {
        return CAPPED_POWERED;
      }
      
      public String visitCashOrNothingPayoffStyle(CashOrNothingPayoffStyle payoffStyle) {
        return CASH_OR_NOTHING;
      }

      @Override
      public String visitFixedStrikePayoffStyle(FixedStrikePayoffStyle payoffStyle) {
        return FIXED_STRIKE;
      }

      @Override
      public String visitPoweredPayoffStyle(PoweredPayoffStyle payoffStyle) {
        return POWERED;
      }

      @Override
      public String visitVanillaPayoffStyle(VanillaPayoffStyle payoffStyle) {
        return VANILLA;
      }

    });
  }

}
