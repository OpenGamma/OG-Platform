/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube.defaultproperties;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Default properties for SABR functions.
 * @deprecated The functions to which these defaults apply are deprecated.
 */
@Deprecated
public class SABRNoExtrapolationDefaults extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(SABRNoExtrapolationDefaults.class);
  /** The value requirements for which these defaults apply */
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY,
    ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY,
    ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY,
    ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.VEGA_QUOTE_CUBE
  };
  /** The SABR surface fitting method */
  private final String _fittingMethod;
  /**
   * A map from currency to (curve config, cube definition, cube specification, forward swap surface definition,
   * forward swap surface specification) names
   */
  private final Map<String, List<String>> _currencyAndConfigNames;

  /**
   * @param fittingMethod The fitting method name, not null
   * @param currencyAndConfigNames A list of either (currency, curve config, cube) triples or
   * (currency, cube definition name, cube specification name, forward surface definition name,
   * forward surface specification name) tuples, not null
   */
  public SABRNoExtrapolationDefaults(final String fittingMethod, final String... currencyAndConfigNames) {
    super(FinancialSecurityTypes.SWAPTION_SECURITY
        .or(FinancialSecurityTypes.SWAP_SECURITY)
        .or(FinancialSecurityTypes.CAP_FLOOR_SECURITY)
        .or(FinancialSecurityTypes.CAP_FLOOR_CMS_SPREAD_SECURITY),
        true);
    ArgumentChecker.notNull(fittingMethod, "fittingMethod");
    ArgumentChecker.notNull(currencyAndConfigNames, "currencyAndConfigNames");
    _fittingMethod = fittingMethod;
    final int nConfigs = currencyAndConfigNames.length;
    _currencyAndConfigNames = new HashMap<>();
    boolean oldConfigs = true;
    ArgumentChecker.isTrue(nConfigs % 3 == 0, "Incorrect number of default arguments");
    for (int i = 0; i < nConfigs; i += 3) {
      // Sets cube definition and specification and forward surface definition and specification names equal
      // to the argument after the curve config. This will not work correctly all of the time (e.g. if some
      // of the cube / surface config names could be parsed as a currency ISO. This code is here to maintain
      // backwards compatibility with code in SABRFunction that did not set these properties explicitly
      try {
        Currency.of(currencyAndConfigNames[i]);
      } catch (final IllegalArgumentException e) {
        oldConfigs = false;
        break;
      }
    }
    if (oldConfigs) {
      for (int i = 0; i < nConfigs; i += 3) {
        final String cubeAndSurfaceName = currencyAndConfigNames[i + 2];
        final List<String> configs = Arrays.asList(currencyAndConfigNames[i + 1], cubeAndSurfaceName, cubeAndSurfaceName,
            cubeAndSurfaceName, cubeAndSurfaceName);
        _currencyAndConfigNames.put(currencyAndConfigNames[i], configs);
      }
    } else {
      ArgumentChecker.isTrue(nConfigs % 6 == 0, "Incorrect number of default arguments");
      for (int i = 0; i < nConfigs; i += 6) {
        final List<String> configs = Arrays.asList(currencyAndConfigNames[i + 1], currencyAndConfigNames[i + 2], currencyAndConfigNames[i + 3],
            currencyAndConfigNames[i + 4], currencyAndConfigNames[i + 5]);
        _currencyAndConfigNames.put(currencyAndConfigNames[i], configs);
      }
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getSecurity();
    if (security instanceof SwapSecurity) {
      if (!InterestRateInstrumentType.isFixedIncomeInstrumentType((SwapSecurity) security)) {
        return false;
      }
      final InterestRateInstrumentType type = SwapSecurityUtils.getSwapType((SwapSecurity) security);
      if ((type != InterestRateInstrumentType.SWAP_FIXED_CMS) &&
          (type != InterestRateInstrumentType.SWAP_CMS_CMS) &&
          (type != InterestRateInstrumentType.SWAP_IBOR_CMS)) {
        return false;
      }
    }
    final String currencyName = FinancialSecurityUtils.getCurrency(security).getCode();
    return _currencyAndConfigNames.containsKey(currencyName);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      defaults.addValuePropertyName(valueRequirement, SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION);
      defaults.addValuePropertyName(valueRequirement, SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION);
      defaults.addValuePropertyName(valueRequirement, SurfaceAndCubePropertyNames.PROPERTY_SURFACE_DEFINITION);
      defaults.addValuePropertyName(valueRequirement, SurfaceAndCubePropertyNames.PROPERTY_SURFACE_SPECIFICATION);
      defaults.addValuePropertyName(valueRequirement, SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue,
      final String propertyName) {
    final String currencyName = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    if (!_currencyAndConfigNames.containsKey(currencyName)) {
      s_logger.error("Could not get configs for currency " + currencyName + "; should never happen");
      return null;
    }
    if (SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD.equals(propertyName)) {
      return Collections.singleton(_fittingMethod);
    }
    final List<String> configs = _currencyAndConfigNames.get(currencyName);
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(configs.get(0));
    }
    if (SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION.equals(propertyName)) {
      return Collections.singleton(configs.get(1));
    }
    if (SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION.equals(propertyName)) {
      return Collections.singleton(configs.get(2));
    }
    if (SurfaceAndCubePropertyNames.PROPERTY_SURFACE_DEFINITION.equals(propertyName)) {
      return Collections.singleton(configs.get(3));
    }
    if (SurfaceAndCubePropertyNames.PROPERTY_SURFACE_SPECIFICATION.equals(propertyName)) {
      return Collections.singleton(configs.get(4));
    }
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.SABR_FITTING_DEFAULTS;
  }

}
