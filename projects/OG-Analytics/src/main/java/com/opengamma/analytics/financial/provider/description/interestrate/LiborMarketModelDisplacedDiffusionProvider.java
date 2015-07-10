/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Interface for LMM parameters in one currency and multi-curves provider.
 */
public class LiborMarketModelDisplacedDiffusionProvider implements LiborMarketModelDisplacedDiffusionProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderInterface _multicurveProvider;
  /**
   * The LMM model parameters.
   */
  private final LiborMarketModelDisplacedDiffusionParameters _parameters;
  /**
   * The currency for which the LMM parameters are valid (LMM on the discounting curve).
   */
  private final Currency _ccy;

  /**
   * Constructor from exiting multicurveProvider and LMM parameters. The given provider and parameters are used for the new provider (the same maps are used, not copied).
   * @param multicurves The multi-curves provider, not null
   * @param parameters The LMM parameters, not null
   * @param ccy The currency for which the LMM parameters are valid (LMM on the discounting curve), not null
   */
  public LiborMarketModelDisplacedDiffusionProvider(final MulticurveProviderInterface multicurves, final LiborMarketModelDisplacedDiffusionParameters parameters,
      final Currency ccy) {
    ArgumentChecker.notNull(multicurves, "multicurves");
    ArgumentChecker.notNull(parameters, "parameters");
    ArgumentChecker.notNull(ccy, "ccy");
    _multicurveProvider = multicurves;
    _parameters = parameters;
    _ccy = ccy;
  }

  /**
   * Create a new copy of the provider.
   * @return The bundle.
   */
  @Override
  public LiborMarketModelDisplacedDiffusionProvider copy() {
    final MulticurveProviderInterface multicurveProvider = _multicurveProvider.copy();
    return new LiborMarketModelDisplacedDiffusionProvider(multicurveProvider, getLMMParameters(), getLMMCurrency());
  }

  /**
   * Returns the LMM parameters.
   * @return The parameters.
   */
  @Override
  public LiborMarketModelDisplacedDiffusionParameters getLMMParameters() {
    return _parameters;
  }

  /**
   * Returns the currency for which the LMM parameters are valid (LMM on the discounting curve).
   * @return The currency.
   */
  @Override
  public Currency getLMMCurrency() {
    return _ccy;
  }

  /**
   * Returns the MulticurveProvider from which the HullWhiteOneFactorProvider is composed.
   * @return The multi-curves provider.
   */
  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _multicurveProvider;
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    return _multicurveProvider.parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _multicurveProvider.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _multicurveProvider.getAllCurveNames();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _ccy.hashCode();
    result = prime * result + _multicurveProvider.hashCode();
    result = prime * result + _parameters.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof LiborMarketModelDisplacedDiffusionProvider)) {
      return false;
    }
    final LiborMarketModelDisplacedDiffusionProvider other = (LiborMarketModelDisplacedDiffusionProvider) obj;
    if (!ObjectUtils.equals(_ccy, other._ccy)) {
      return false;
    }
    if (!ObjectUtils.equals(_multicurveProvider, other._multicurveProvider)) {
      return false;
    }
    if (!ObjectUtils.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

}
