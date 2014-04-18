/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube.defaultproperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 * @deprecated The functions in this package are deprecated
 */
@Deprecated
public class DefaultPropertiesFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Currency-specific data.
   */
  public static class CurrencyInfo implements InitializingBean {
    /** The logger */
    private static final Logger s_logger = LoggerFactory.getLogger(DefaultPropertiesFunctions.CurrencyInfo.class);
    /** The curve configuration name */
    private String _curveConfiguration;
    /** The cube name. Left in for backwards compatibility */
    private String _cubeName;
    /** True if the cube name is set directly */
    private boolean _isCubeNameParameterSet;
    /** The cube definition name */
    private String _cubeDefinitionName;
    /** The cube specification name */
    private String _cubeSpecificationName;
    /** The forward surface definition name */
    private String _surfaceDefinitionName;
    /** The forward surface specification name */
    private String _surfaceSpecificationName;

    /**
     * Gets the curve configuration name.
     * @return The curve configuration name
     */
    public String getCurveConfiguration() {
      return _curveConfiguration;
    }

    /**
     * Sets the curve configuration name.
     * @param curveConfiguration The curve configuration name
     */
    public void setCurveConfiguration(final String curveConfiguration) {
      _curveConfiguration = curveConfiguration;
    }

    /**
     * Gets the cube name. Should not be used.
     * @return The cube name
     * @deprecated Should not be used; get the cube definition and specification
     * and forward surface definition and specification names with the individual
     * methods.
     */
    @Deprecated
    public String getCubeName() {
      return _cubeName;
    }

    /**
     * Sets the cube definition and specification and forward surface definition
     * and specification names to the same value. Should not be used.
     * @param cubeName The cube name
     * @deprecated Should not be used; set the cube definition and specification
     * and forward surface definition and specification names with the individual
     * methods.
     */
    @Deprecated
    public void setCubeName(final String cubeName) {
      _isCubeNameParameterSet = true;
      _cubeName = cubeName;
      if (_cubeDefinitionName != null) {
        s_logger.error("Cube definition name was already set using the setCubeDefinitionName() method. This will" +
            " almost certainly result in unexpected behaviour");
        _cubeDefinitionName = cubeName;
      }
      if (_cubeSpecificationName != null) {
        s_logger.error("Cube specification name was already set using the setCubeSpecificationName() method. This will" +
            " almost certainly result in unexpected behaviour");
        _cubeSpecificationName = cubeName;
      }
      if (_surfaceDefinitionName != null) {
        s_logger.error("Surface definition name was already set using the setSurfaceDefinitionName() method. This will" +
            " almost certainly result in unexpected behaviour");
        _surfaceDefinitionName = cubeName;
      }
      if (_surfaceSpecificationName != null) {
        s_logger.error("Surface specification name was already set using the setSurfaceSpecificationName() method. This will" +
            " almost certainly result in unexpected behaviour");
        _surfaceSpecificationName = cubeName;
      }
    }

    /**
     * Returns true if the cubeName parameter is set.
     * @return True if the cubeName parameter is set
     */
    public boolean isCubeNameParameterSet() {
      return _isCubeNameParameterSet;
    }

    /**
     * Gets the cube definition name.
     * @return The cube definition name
     */
    public String getCubeDefinitionName() {
      return _cubeDefinitionName;
    }

    /**
     * Sets the cube definition name.
     * @param cubeDefinitionName The cube definition name.
     */
    public void setCubeDefinitionName(final String cubeDefinitionName) {
      if (_isCubeNameParameterSet) {
        s_logger.error("Cube definition name was already set using the deprecated setCubeName() method. This will" +
            " almost certainly result in unexpected behaviour");
        _cubeDefinitionName = cubeDefinitionName;
        return;
      }
      _cubeDefinitionName = cubeDefinitionName;
    }

    /**
     * Gets the cube specification name.
     * @return The cube specification name
     */
    public String getCubeSpecificationName() {
      return _cubeSpecificationName;
    }

    /**
     * Sets the cube specification name.
     * @param cubeSpecificationName The cube specification name.
     */
    public void setCubeSpecificationName(final String cubeSpecificationName) {
      if (_isCubeNameParameterSet) {
        s_logger.error("Cube specification name was already set using the deprecated setCubeName() method. This will" +
            " almost certainly result in unexpected behaviour");
        _cubeSpecificationName = cubeSpecificationName;
        return;
      }
      _cubeSpecificationName = cubeSpecificationName;
    }

    /**
     * Gets the forward surface definition name.
     * @return The surface definition name
     */
    public String getSurfaceDefinitionName() {
      return _surfaceDefinitionName;
    }

    /**
     * Sets the forward surface definition name.
     * @param surfaceDefinitionName The surface definition name.
     */
    public void setSurfaceDefinitionName(final String surfaceDefinitionName) {
      if (_isCubeNameParameterSet) {
        s_logger.error("Surface definition name was already set using the deprecated setCubeName() method. This will" +
            " almost certainly result in unexpected behaviour");
        _surfaceDefinitionName = surfaceDefinitionName;
        return;
      }
      _surfaceDefinitionName = surfaceDefinitionName;
    }

    /**
     * Gets the surface specification name.
     * @return The surface specification name
     */
    public String getSurfaceSpecificationName() {
      return _surfaceSpecificationName;
    }

    /**
     * Sets the surface specification name.
     * @param surfaceSpecificationName The surface specification name.
     */
    public void setSurfaceSpecificationName(final String surfaceSpecificationName) {
      if (_isCubeNameParameterSet) {
        s_logger.error("Surface specification name was already set using the deprecated setCubeName() method. This will" +
            " almost certainly result in unexpected behaviour");
        _surfaceSpecificationName = surfaceSpecificationName;
        return;
      }
      _surfaceSpecificationName = surfaceSpecificationName;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNullInjected(getCubeDefinitionName(), "cubeDefinitionName");
      ArgumentChecker.notNullInjected(getCubeSpecificationName(), "cubeSpecificationName");
      ArgumentChecker.notNullInjected(getSurfaceDefinitionName(), "surfaceDefinitionName");
      ArgumentChecker.notNullInjected(getSurfaceSpecificationName(), "surfaceSpecificationName");
      ArgumentChecker.notNullInjected(getCurveConfiguration(), "curveConfiguration");
    }
  }

  /** The per-currency info */
  private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<>();
  private String _fittingMethod = SmileFittingPropertyNamesAndValues.NON_LINEAR_LEAST_SQUARES;
  private String _xInterpolator = Interpolator1DFactory.LINEAR;
  private String _xLeftExtrapolator = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
  private String _xRightExtrapolator = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
  private String _yInterpolator = Interpolator1DFactory.LINEAR;
  private String _yLeftExtrapolator = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
  private String _yRightExtrapolator = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
  private double _cutOff = 0.07;
  private double _mu = 10.0;

  public void setPerCurrencyInfo(final Map<String, CurrencyInfo> perCurrencyInfo) {
    _perCurrencyInfo.clear();
    _perCurrencyInfo.putAll(perCurrencyInfo);
  }

  public Map<String, CurrencyInfo> getPerCurrencyInfo() {
    return _perCurrencyInfo;
  }


  public void setCurrencyInfo(final String currency, final CurrencyInfo info) {
    _perCurrencyInfo.put(currency, info);
  }

  public CurrencyInfo getCurrencyInfo(final String currency) {
    return _perCurrencyInfo.get(currency);
  }

  public void setFittingMethod(final String fittingMethod) {
    _fittingMethod = fittingMethod;
  }

  public String getFittingMethod() {
    return _fittingMethod;
  }

  public String getXInterpolator() {
    return _xInterpolator;
  }

  public void setXInterpolator(final String xInterpolator) {
    _xInterpolator = xInterpolator;
  }

  public String getXLeftExtrapolator() {
    return _xLeftExtrapolator;
  }

  public void setXLeftExtrapolator(final String xLeftExtrapolator) {
    _xLeftExtrapolator = xLeftExtrapolator;
  }

  public String getXRightExtrapolator() {
    return _xRightExtrapolator;
  }

  public void setXRightExtrapolator(final String xRightExtrapolator) {
    _xRightExtrapolator = xRightExtrapolator;
  }

  public String getYInterpolator() {
    return _yInterpolator;
  }

  public void setYInterpolator(final String yInterpolator) {
    _yInterpolator = yInterpolator;
  }

  public String getYLeftExtrapolator() {
    return _yLeftExtrapolator;
  }

  public void setYLeftExtrapolator(final String yLeftExtrapolator) {
    _yLeftExtrapolator = yLeftExtrapolator;
  }

  public String getYRightExtrapolator() {
    return _yRightExtrapolator;
  }

  public void setYRightExtrapolator(final String yRightExtrapolator) {
    _yRightExtrapolator = yRightExtrapolator;
  }

  public void setCutOff(final double cutOff) {
    _cutOff = cutOff;
  }

  public double getCutOff() {
    return _cutOff;
  }

  public void setMu(final double mu) {
    _mu = mu;
  }

  public double getMu() {
    return _mu;
  }

  @Override
  public void afterPropertiesSet() {
    ArgumentChecker.notNullInjected(getFittingMethod(), "fittingMethod");
    ArgumentChecker.notNullInjected(getXInterpolator(), "xInterpolator");
    ArgumentChecker.notNullInjected(getXLeftExtrapolator(), "xLeftExtrapolator");
    ArgumentChecker.notNullInjected(getXRightExtrapolator(), "xRightExtrapolator");
    ArgumentChecker.notNullInjected(getYInterpolator(), "yInterpolator");
    ArgumentChecker.notNullInjected(getYLeftExtrapolator(), "yLeftExtrapolator");
    ArgumentChecker.notNullInjected(getYRightExtrapolator(), "yRightExtrapolator");
    super.afterPropertiesSet();
  }

  /**
   * Adds defaults for SABR calculations without extrapolation.
   * @param functions The functions
   */
  protected void addNoExtrapolationDefaults(final List<FunctionConfiguration> functions) {
    final List<String> args = new ArrayList<>();
    args.add(getFittingMethod());
    for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
      if (e.getValue().isCubeNameParameterSet()) {
        args.add(e.getKey());
        args.add(e.getValue().getCurveConfiguration());
        args.add(e.getValue().getCubeName());
      } else {
        args.add(e.getKey());
        args.add(e.getValue().getCurveConfiguration());
        args.add(e.getValue().getCubeDefinitionName());
        args.add(e.getValue().getCubeSpecificationName());
        args.add(e.getValue().getSurfaceDefinitionName());
        args.add(e.getValue().getSurfaceSpecificationName());
      }
    }
    functions.add(functionConfiguration(SABRNoExtrapolationDefaults.class, args.toArray(new String[args.size()])));
  }

  /**
   * Adds defaults for the calculation of vega using SABR without extrapolation.
   * @param functions The functions
   */
  protected void addNoExtrapolationVegaDefaults(final List<FunctionConfiguration> functions) {
    final List<String> args = new ArrayList<>();
    args.add(getFittingMethod());
    args.add(getXInterpolator());
    args.add(getXLeftExtrapolator());
    args.add(getXRightExtrapolator());
    args.add(getYInterpolator());
    args.add(getYLeftExtrapolator());
    args.add(getYRightExtrapolator());
    for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
      if (e.getValue().isCubeNameParameterSet()) {
        args.add(e.getKey());
        args.add(e.getValue().getCurveConfiguration());
        args.add(e.getValue().getCubeName());
      } else {
        args.add(e.getKey());
        args.add(e.getValue().getCurveConfiguration());
        args.add(e.getValue().getCubeDefinitionName());
        args.add(e.getValue().getCubeSpecificationName());
        args.add(e.getValue().getSurfaceDefinitionName());
        args.add(e.getValue().getSurfaceSpecificationName());
      }
    }
    functions.add(functionConfiguration(SABRNoExtrapolationVegaDefaults.class, args.toArray(new String[args.size()])));
  }

  /**
   * Adds defaults for SABR calculations with right extrapolation.
   * @param functions The functions
   */
  protected void addRightExtrapolationDefaults(final List<FunctionConfiguration> functions) {
    final List<String> args = new ArrayList<>();
    args.add(getFittingMethod());
    args.add(Double.toString(getCutOff()));
    args.add(Double.toString(getMu()));
    for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
      if (e.getValue().isCubeNameParameterSet()) {
        args.add(e.getKey());
        args.add(e.getValue().getCurveConfiguration());
        args.add(e.getValue().getCubeName());
      } else {
        args.add(e.getKey());
        args.add(e.getValue().getCurveConfiguration());
        args.add(e.getValue().getCubeDefinitionName());
        args.add(e.getValue().getCubeSpecificationName());
        args.add(e.getValue().getSurfaceDefinitionName());
        args.add(e.getValue().getSurfaceSpecificationName());
      }
    }
    functions.add(functionConfiguration(SABRRightExtrapolationDefaults.class, args.toArray(new String[args.size()])));
  }

  /**
   * Adds defaults for the calculation of vega using SABR with right extrapolation.
   * @param functions The functions
   */
  protected void addRightExtrapolationVegaDefaults(final List<FunctionConfiguration> functions) {
    final List<String> args = new ArrayList<>();
    args.add(getFittingMethod());
    args.add(Double.toString(getCutOff()));
    args.add(Double.toString(getMu()));
    args.add(getXInterpolator());
    args.add(getXLeftExtrapolator());
    args.add(getXRightExtrapolator());
    args.add(getYInterpolator());
    args.add(getYLeftExtrapolator());
    args.add(getYRightExtrapolator());
    for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
      if (e.getValue().isCubeNameParameterSet()) {
        args.add(e.getKey());
        args.add(e.getValue().getCurveConfiguration());
        args.add(e.getValue().getCubeName());
      } else {
        args.add(e.getKey());
        args.add(e.getValue().getCurveConfiguration());
        args.add(e.getValue().getCubeDefinitionName());
        args.add(e.getValue().getCubeSpecificationName());
        args.add(e.getValue().getSurfaceDefinitionName());
        args.add(e.getValue().getSurfaceSpecificationName());
      }
    }
    functions.add(functionConfiguration(SABRRightExtrapolationVegaDefaults.class, args.toArray(new String[args.size()])));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    if (!getPerCurrencyInfo().isEmpty()) {
      addNoExtrapolationDefaults(functions);
      addNoExtrapolationVegaDefaults(functions);
      addRightExtrapolationDefaults(functions);
      addRightExtrapolationVegaDefaults(functions);
    }
  }

}
