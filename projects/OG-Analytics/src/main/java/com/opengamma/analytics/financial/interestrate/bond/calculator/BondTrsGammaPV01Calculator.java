/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

/**
 * Calculates the PV01s for a bond total return swap.
 */
/** A singleton instance */
/** The PV01 calculator */
/**
   * Gets the singleton instance.
   * @return The singleton instance
   */
/**
   * Private constructor.
   */
/**
   * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
   *
   * Please see distribution for license.
   */
import java.util.Map;

import com.opengamma.analytics.ShiftType;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurveUtils;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Returns the change in PV01 of an instrument due to a parallel 1bp move of <b>all</b> the curves to which the bond total
 * return swap is sensitive. The value is returned in the currency of the asset.
 */
public final class BondTrsGammaPV01Calculator extends InstrumentDerivativeVisitorAdapter<ParameterIssuerProviderInterface, Double> {
  /** The singleton instance */
  private static final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, Double> INSTANCE =
      new BondTrsGammaPV01Calculator();
  /**
   * The size of the scaling: 1 basis point.
   */
  private static final double BP1 = 1.0E-4;

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, Double> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private BondTrsGammaPV01Calculator() {

  }

  /**
   * Calculates the change in PV01 of an instrument due to a parallel move of each yield curve the instrument is sensitive to, scaled so that the move is 1bp.
   * @param bondTrs The instrument, not null
   * @param data The curve data provider, not null
   * @return The scaled sensitivity for each curve/currency.
   */
  @Override
  public Double visitBondTotalReturnSwap(final BondTotalReturnSwap bondTrs, final ParameterIssuerProviderInterface data) {
    ArgumentChecker.notNull(bondTrs, "bondTrs");
    ArgumentChecker.notNull(data, "data");
    final ParameterIssuerProviderInterface bumped = getBumpedProvider(data);
    final ReferenceAmount<Pair<String, Currency>> pv01 = bondTrs.accept(BondTrsPV01Calculator.getInstance(), data);
    final ReferenceAmount<Pair<String, Currency>> up = bondTrs.accept(BondTrsPV01Calculator.getInstance(), bumped);
    final Currency assetCurrency = bondTrs.getAsset().getCurrency();
    double gammaPV01 = 0;
    for (final Map.Entry<Pair<String, Currency>, Double> entry : pv01.getMap().entrySet()) {
      final Pair<String, Currency> bumpedNameCurrency = Pairs.of(entry.getKey().getFirst() + YieldCurveUtils.PARALLEL_SHIFT_NAME, entry.getKey().getSecond());
      if (!(up.getMap().containsKey(bumpedNameCurrency))) {
        throw new IllegalStateException("Have bumped PV01 for curve / currency pair " + entry.getKey() + " but no PV01");
      }
      final Currency pv01Currency = entry.getKey().getSecond();
      final double fxRate = data.getMulticurveProvider().getFxRate(pv01Currency, assetCurrency);
      gammaPV01 += fxRate * (up.getMap().get(bumpedNameCurrency) - entry.getValue()) / BP1;
    }
    return gammaPV01;
  }

  /**
   * Bumps every curve in a provider. This method will be replaced when the curve providers
   * have been refactored.
   * @param data The curves
   * @return A provider with each curve bumped by +1 bp
   */
  private static ParameterIssuerProviderInterface getBumpedProvider(final ParameterIssuerProviderInterface data) {
    if (data instanceof IssuerProviderDiscount) {
      final IssuerProviderDiscount discount = ((IssuerProviderDiscount) data);
      final MulticurveProviderDiscount multicurveProvider = discount.getMulticurveProvider();
      final IssuerProviderDiscount bumped = new IssuerProviderDiscount(new MulticurveProviderDiscount(multicurveProvider.getFxRates()));
      for (final Map.Entry<Currency, YieldAndDiscountCurve> entry : multicurveProvider.getDiscountingCurves().entrySet()) {
        if (!(entry.getValue() instanceof YieldCurve)) {
          throw new IllegalArgumentException("Can only bump YieldCurves");
        }
        bumped.getMulticurveProvider().setCurve(entry.getKey(), YieldCurveUtils.withParallelShift((YieldCurve) entry.getValue(), BP1, ShiftType.ABSOLUTE));
      }
      for (final Map.Entry<IborIndex, YieldAndDiscountCurve> entry : multicurveProvider.getForwardIborCurves().entrySet()) {
        if (!(entry.getValue() instanceof YieldCurve)) {
          throw new IllegalArgumentException("Can only bump YieldCurves");
        }
        bumped.getMulticurveProvider().setCurve(entry.getKey(), YieldCurveUtils.withParallelShift((YieldCurve) entry.getValue(), BP1, ShiftType.ABSOLUTE));
      }
      for (final Map.Entry<IndexON, YieldAndDiscountCurve> entry : multicurveProvider.getForwardONCurves().entrySet()) {
        if (!(entry.getValue() instanceof YieldCurve)) {
          throw new IllegalArgumentException("Can only bump YieldCurves");
        }
        bumped.getMulticurveProvider().setCurve(entry.getKey(), YieldCurveUtils.withParallelShift((YieldCurve) entry.getValue(), BP1, ShiftType.ABSOLUTE));
      }
      for (final Map.Entry<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> entry : discount.getIssuerCurves().entrySet()) {
        if (!(entry.getValue() instanceof YieldCurve)) {
          throw new IllegalArgumentException("Can only bump YieldCurves");
        }
        bumped.setCurve(entry.getKey(), YieldCurveUtils.withParallelShift((YieldCurve) entry.getValue(), BP1, ShiftType.ABSOLUTE));
      }
      return bumped;
    }
    throw new UnsupportedOperationException("Cannot bump curves of type " + data.getClass());
  }
}
