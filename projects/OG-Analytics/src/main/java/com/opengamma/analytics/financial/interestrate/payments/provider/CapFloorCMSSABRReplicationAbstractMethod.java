/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *  Class used to compute the price of a CMS cap/floor by swaption replication in (extended) SABR framework.
 */
public abstract class CapFloorCMSSABRReplicationAbstractMethod {

  /**
   * Range of the integral. Used only for caps. Represent the approximation of infinity in the strike dimension.
   * The range is [strike, strike+integrationInterval].
   */
  private final double _integrationInterval;
  /**
   * Minimal number of integration steps in the replication.
   */
  private final int _nbIteration = 10;

  /**
   * Constructor of the CMS cap/floor replication method with the integration range. 
   * @param integrationInterval Integration range.
   */
  public CapFloorCMSSABRReplicationAbstractMethod(final double integrationInterval) {
    _integrationInterval = integrationInterval;
  }

  /**
   * Gets the integration interval.
   * @return The integration interval.
   */
  public double getIntegrationInterval() {
    return _integrationInterval;
  }

  /**
   * Gets the minimal number of iterations for the numerical integration.
   * @return The number.
   */
  public int getNbIteration() {
    return _nbIteration;
  }

  /**
   * Compute the present value of a CMS cap/floor by replication in (extended) SABR framework.
   * @param cmsCapFloor The CMS cap/floor.
   * @param sabrData The SABR data bundle.
   * @return The present value.
   */
  public abstract MultipleCurrencyAmount presentValue(final CapFloorCMS cmsCapFloor, final SABRSwaptionProviderInterface sabrData);

  /**
   * Computes the present value sensitivity to the yield curves of a CMS cap/floor in (extended) SABR framework.
   * @param cmsCapFloor The CMS cap/floor.
   * @param sabrData The SABR data bundle. The SABR function need to be the Hagan function.
   * @return The present value sensitivity to curves.
   */
  public abstract MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CapFloorCMS cmsCapFloor, final SABRSwaptionProviderInterface sabrData);

  /**
   * Computes the present value sensitivity to the SABR parameters of a CMS cap/floor in (extended) SABR framework.
   * @param cmsCapFloor The CMS cap/floor.
   * @param sabrData The SABR data bundle. The SABR function need to be the Hagan function.
   * @return The present value sensitivity to SABR parameters.
   */
  public abstract PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final CapFloorCMS cmsCapFloor, final SABRSwaptionProviderInterface sabrData);

  /**
   * Computes the present value sensitivity to the strike of a CMS cap/floor in (extended) SABR framework.
   * @param cmsCapFloor The CMS cap/floor.
   * @param sabrData The SABR data bundle. The SABR function need to be the Hagan function.
   * @return The present value sensitivity to strike.
   */
  public abstract double presentValueStrikeSensitivity(final CapFloorCMS cmsCapFloor, final SABRSwaptionProviderInterface sabrData);

}
