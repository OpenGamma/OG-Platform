/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import com.opengamma.financial.security.option.AssetOrNothingPayoffStyle;
import com.opengamma.financial.security.option.AsymmetricPoweredPayoffStyle;
import com.opengamma.financial.security.option.BarrierPayoffStyle;
import com.opengamma.financial.security.option.CappedPoweredPayoffStyle;
import com.opengamma.financial.security.option.CashOrNothingPayoffStyle;
import com.opengamma.financial.security.option.ExtremeSpreadPayoffStyle;
import com.opengamma.financial.security.option.FadeInPayoffStyle;
import com.opengamma.financial.security.option.FixedStrikeLookbackPayoffStyle;
import com.opengamma.financial.security.option.FloatingStrikeLookbackPayoffStyle;
import com.opengamma.financial.security.option.GapPayoffStyle;
import com.opengamma.financial.security.option.PayoffStyleVisitor;
import com.opengamma.financial.security.option.PoweredPayoffStyle;
import com.opengamma.financial.security.option.SimpleChooserPayoffStyle;
import com.opengamma.financial.security.option.SupersharePayoffStyle;
import com.opengamma.financial.security.option.VanillaPayoffStyle;
import com.opengamma.masterdb.security.hibernate.EnumUserType;

/**
 * Custom Hibernate usertype for the OptionPayoffStyle enum
 */
public class OptionPayoffStyleUserType extends EnumUserType<OptionPayoffStyle> {

  private static final String ASSET_OR_NOTHING = "Asset-or-Nothing";
  private static final String ASYMMETRIC_POWERED = "Asymmetric Powered";
  private static final String BARRIER = "Barrier";
  private static final String CAPPED_POWERED = "Capped Powered";
  private static final String CASH_OR_NOTHING = "Cash-or-Nothing";
  private static final String EXTREME_SPREAD = "Extreme Spread";
  private static final String FADE_IN = "Fade-In";
  private static final String FIXED_STRIKE_LOOKBACK = "Fixed-Strike Lookback";
  private static final String FLOATING_STRIKE_LOOKBACK = "Floating-Strike Lookback";
  private static final String GAP = "Gap";
  private static final String POWERED = "Powered";
  private static final String SIMPLE_CHOOSER = "Simple Chooser";
  private static final String SUPERSHARE = "Supershare";
  private static final String VANILLA = "Vanilla";

  public OptionPayoffStyleUserType() {
    super(OptionPayoffStyle.class, OptionPayoffStyle.values());
  }

  @Override
  protected String enumToStringNoCache(final OptionPayoffStyle value) {
    return value.accept(new PayoffStyleVisitor<String>() {

      @Override
      public String visitAssetOrNothingPayoffStyle(final AssetOrNothingPayoffStyle payoffStyle) {
        return ASSET_OR_NOTHING;
      }

      @Override
      public String visitAsymmetricPoweredPayoffStyle(final AsymmetricPoweredPayoffStyle payoffStyle) {
        return ASYMMETRIC_POWERED;
      }

      @Override
      public String visitBarrierPayoffStyle(final BarrierPayoffStyle payoffStyle) {
        return BARRIER;
      }

      @Override
      public String visitCappedPoweredPayoffStyle(final CappedPoweredPayoffStyle payoffStyle) {
        return CAPPED_POWERED;
      }

      @Override
      public String visitCashOrNothingPayoffStyle(final CashOrNothingPayoffStyle payoffStyle) {
        return CASH_OR_NOTHING;
      }

      @Override
      public String visitExtremeSpreadPayoffStyle(final ExtremeSpreadPayoffStyle payoffStyle) {
        return EXTREME_SPREAD;
      }
      
      @Override
      public String visitFadeInPayoffStyle(final FadeInPayoffStyle payoffStyle) {
        return FADE_IN;
      }

      @Override
      public String visitFixedStrikeLookbackPayoffStyle(final FixedStrikeLookbackPayoffStyle payoffStyle) {
        return FIXED_STRIKE_LOOKBACK;
      }

      @Override
      public String visitFloatingStrikeLookbackPayoffStyle(final FloatingStrikeLookbackPayoffStyle payoffStyle) {
        return FLOATING_STRIKE_LOOKBACK;
      }

      @Override
      public String visitGapPayoffStyle(final GapPayoffStyle payoffStyle) {
        return GAP;
      }

      @Override
      public String visitPoweredPayoffStyle(final PoweredPayoffStyle payoffStyle) {
        return POWERED;
      }

      @Override
      public String visitSimpleChooserPayoffStyle(final SimpleChooserPayoffStyle payoffStyle) {
        return SIMPLE_CHOOSER;
      }

      @Override
      public String visitSupersharePayoffStyle(final SupersharePayoffStyle payoffStyle) {
        return SUPERSHARE;
      }

      @Override
      public String visitVanillaPayoffStyle(final VanillaPayoffStyle payoffStyle) {
        return VANILLA;
      }

    });
  }

}
