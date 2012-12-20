/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;
 
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.option.PayoffStyleVisitor;

/**
 * Payoff style of the option.
 */
public enum OptionPayoffStyle {

  /** Asset or Nothing */
  ASSET_OR_NOTHING,
  /** Asymmetric powered */
  ASYMMETRIC_POWERED,
  /** Barrier */
  BARRIER,
  /** Capped Powered*/
  CAPPED_POWERED,
  /** Cash-or-Nothing*/
  CASH_OR_NOTHING,
  /** Extreme spread*/
  EXTREME_SPREAD,
  /** Fade-in */
  FADE_IN,
  /** Fixed-strike lookback*/
  FIXED_STRIKE_LOOKBACK,
  /** Floating-strike lookback*/
  FLOATING_STRIKE_LOOKBACK,
  /** Gap */
  GAP,
  /** Powered */
  POWERED,
  /** Simple chooser */
  SIMPLE_CHOOSER,
  /** Supershare */
  SUPERSHARE,
  /** Vanilla */
  VANILLA;

  public <T> T accept(final PayoffStyleVisitor<T> visitor) {
    switch (this) {
      case ASSET_OR_NOTHING:
        return visitor.visitAssetOrNothingPayoffStyle(null);
      case ASYMMETRIC_POWERED:
        return visitor.visitAsymmetricPoweredPayoffStyle(null);
      case BARRIER:
        return visitor.visitBarrierPayoffStyle(null);
      case CAPPED_POWERED:
        return visitor.visitCappedPoweredPayoffStyle(null);
      case CASH_OR_NOTHING:
        return visitor.visitCashOrNothingPayoffStyle(null);
      case EXTREME_SPREAD:
        return visitor.visitExtremeSpreadPayoffStyle(null);
      case FADE_IN:
        return visitor.visitFadeInPayoffStyle(null);
      case FIXED_STRIKE_LOOKBACK:
        return visitor.visitFixedStrikeLookbackPayoffStyle(null);
      case FLOATING_STRIKE_LOOKBACK:
        return visitor.visitFloatingStrikeLookbackPayoffStyle(null);
      case GAP:
        return visitor.visitGapPayoffStyle(null);
      case POWERED:
        return visitor.visitPoweredPayoffStyle(null);
      case SIMPLE_CHOOSER:
        return visitor.visitSimpleChooserPayoffStyle(null);
      case SUPERSHARE:
        return visitor.visitSupersharePayoffStyle(null);
      case VANILLA:
        return visitor.visitVanillaPayoffStyle(null);
      default:
        throw new OpenGammaRuntimeException("unexpected enum value " + this);
    }
  }

}
