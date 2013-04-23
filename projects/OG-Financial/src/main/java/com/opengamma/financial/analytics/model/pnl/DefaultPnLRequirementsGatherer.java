/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.money.Currency;

/**
 * PnL requirements gatherer.
 */
public class DefaultPnLRequirementsGatherer implements PnLRequirementsGatherer {

  private final Map<String, String> _curveCalculationConfigs = new HashMap<>();
  private final Map<String, String> _fxCurveCalculationConfigs = new HashMap<>();
  private final Map<String, String> _fxDiscountingCurveNames = new HashMap<>();
  private final Map<String, String> _irFuturesCurveCalculationConfigs = new HashMap<>();
  private String _fxVolSurfaceName = "DEFAULT";
  private String _swaptionVolSurfaceName = "DEFAULT";
  private String _irFutureOptionVolSurfaceName = "DEFAULT";
  private String _bondFutureOptionVolSurfaceName = "DEFAULT";
  private String _fxVolInterpolator = Interpolator1DFactory.DOUBLE_QUADRATIC;
  private String _fxVolLeftExtrapolator = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
  private String _fxVolRightExtrapolator = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;

  protected void addCurveCalculationConfig(final String currency, final String configName) {
    _curveCalculationConfigs.put(currency, configName);
  }

  protected void addFXCurveCalculationConfig(final String currency, final String configName) {
    _fxCurveCalculationConfigs.put(currency, configName);
  }

  protected void addFXDiscountingCurveName(final String currency, final String curveName) {
    _fxDiscountingCurveNames.put(currency, curveName);
  }

  protected void addIRFuturesCurveCalculationConfig(final String currency, final String configName) {
    _irFuturesCurveCalculationConfigs.put(currency, configName);
  }

  protected void setFXVolSurfaceName(final String fxVolSurfaceName) {
    _fxVolSurfaceName = fxVolSurfaceName;
  }

  protected void setSwaptionVolSurfaceName(final String swaptionVolSurfaceName) {
    _swaptionVolSurfaceName = swaptionVolSurfaceName;
  }

  protected void setIRFutureOptionVolSurfaceName(final String irFutureOptionVolSurfaceName) {
    _irFutureOptionVolSurfaceName = irFutureOptionVolSurfaceName;
  }

  protected void setBondFutureOptionVolSurfaceName(final String bondFutureOptionVolSurfaceName) {
    _bondFutureOptionVolSurfaceName = bondFutureOptionVolSurfaceName;
  }

  protected void setFXVolInterpolator(final String fxVolInterpolator) {
    _fxVolInterpolator = fxVolInterpolator;
  }

  protected void setFXVolLeftExtrapolator(final String fxVolLeftExtrapolator) {
    _fxVolLeftExtrapolator = fxVolLeftExtrapolator;
  }

  protected void setFXVolRightExtrapolator(final String fxVolRightExtrapolator) {
    _fxVolRightExtrapolator = fxVolRightExtrapolator;
  }

  @Override
  public Set<ValueRequirement> getFirstOrderRequirements(final FinancialSecurity security, final String samplingPeriod, final String scheduleCalculator, final String samplingFunction,
      final ComputationTargetSpecification targetSpec, final String currency) {
    return security.accept(getFirstOrderRequirements(samplingPeriod, scheduleCalculator, samplingFunction, targetSpec, currency));
  }

  //TODO another visitor that takes desiredValue and uses those properties instead of the static defaults
  protected FinancialSecurityVisitor<Set<ValueRequirement>> getFirstOrderRequirements(final String samplingPeriod, final String scheduleCalculator, final String samplingFunction,
      final ComputationTargetSpecification targetSpec, final String currency) {
    final ValueProperties commonProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriod)
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculator)
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunction).get();
    return new FinancialSecurityVisitorAdapter<Set<ValueRequirement>>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueRequirement> visitSwapSecurity(final SwapSecurity security) {
        final String securityCurrency = FinancialSecurityUtils.getCurrency(security).getCode();
        final String calculationConfig = _curveCalculationConfigs.get(securityCurrency);
        if (calculationConfig == null) {
          throw new OpenGammaRuntimeException("Could not get curve calculation config for " + securityCurrency);
        }
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, calculationConfig)
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES).get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueRequirement> visitFRASecurity(final FRASecurity security) {
        final String securityCurrency = FinancialSecurityUtils.getCurrency(security).getCode();
        final String calculationConfig = _curveCalculationConfigs.get(securityCurrency);
        if (calculationConfig == null) {
          throw new OpenGammaRuntimeException("Could not get curve calculation config for " + securityCurrency);
        }
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, calculationConfig)
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES).get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueRequirement> visitSwaptionSecurity(final SwaptionSecurity security) {
        final String securityCurrency = FinancialSecurityUtils.getCurrency(security).getCode();
        final String calculationConfig = _curveCalculationConfigs.get(securityCurrency);
        if (calculationConfig == null) {
          throw new OpenGammaRuntimeException("Could not get curve calculation config for " + securityCurrency);
        }
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD)
            .with(ValuePropertyNames.SURFACE, _swaptionVolSurfaceName)
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, calculationConfig)
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES).get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueRequirement> visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
        final String securityCurrency = FinancialSecurityUtils.getCurrency(security).getCode();
        final String calculationConfig = _curveCalculationConfigs.get(securityCurrency);
        if (calculationConfig == null) {
          throw new OpenGammaRuntimeException("Could not get curve calculation config for " + securityCurrency);
        }
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, calculationConfig)
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES).get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueRequirement> visitCorporateBondSecurity(final CorporateBondSecurity security) {
        final String securityCurrency = FinancialSecurityUtils.getCurrency(security).getCode();
        final String calculationConfig = _curveCalculationConfigs.get(securityCurrency);
        if (calculationConfig == null) {
          throw new OpenGammaRuntimeException("Could not get curve calculation config for " + securityCurrency);
        }
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, calculationConfig)
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES).get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueRequirement> visitFXForwardSecurity(final FXForwardSecurity security) {
        final String payCurrency = security.getPayCurrency().getCode();
        final String payCalculationConfig = _fxCurveCalculationConfigs.get(payCurrency);
        if (payCalculationConfig == null) {
          throw new OpenGammaRuntimeException("Could not get curve calculation config for " + payCurrency);
        }
        final String payDiscountingCurve = _fxDiscountingCurveNames.get(payCurrency);
        if (payDiscountingCurve == null) {
          throw new OpenGammaRuntimeException("Could not get discounting curve for " + payCurrency);
        }
        final String receiveCurrency = security.getReceiveCurrency().getCode();
        final String receiveCalculationConfig = _fxCurveCalculationConfigs.get(receiveCurrency);
        if (receiveCalculationConfig == null) {
          throw new OpenGammaRuntimeException("Could not get curve calculation config for " + receiveCurrency);
        }
        final String receiveDiscountingCurve = _fxDiscountingCurveNames.get(receiveCurrency);
        if (receiveDiscountingCurve == null) {
          throw new OpenGammaRuntimeException("Could not get discounting curve for " + receiveCurrency);
        }
        final ValueProperties currencyExposureProperties = commonProperties.copy()
            .with(ValuePropertyNames.PAY_CURVE, payDiscountingCurve)
            .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCalculationConfig)
            .with(ValuePropertyNames.RECEIVE_CURVE, receiveDiscountingCurve)
            .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCalculationConfig)
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.FX_CURRENCY_EXPOSURE).get();
        final ValueProperties ycnsProperties = commonProperties.copy()
            .with(ValuePropertyNames.PAY_CURVE, payDiscountingCurve)
            .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCalculationConfig)
            .with(ValuePropertyNames.RECEIVE_CURVE, receiveDiscountingCurve)
            .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCalculationConfig)
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES).get();
        final ValueRequirement fxCurrencyExposure = new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, currencyExposureProperties);
        final ValueRequirement fxYCNSExposure = new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, ycnsProperties);
        return Sets.newHashSet(fxCurrencyExposure, fxYCNSExposure);
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueRequirement> visitFXOptionSecurity(final FXOptionSecurity security) {
        final Currency putCurrency = security.getPutCurrency();
        final Currency callCurrency = security.getCallCurrency();
        final String putCurrencyString = putCurrency.getCode();
        final String callCurrencyString = callCurrency.getCode();
        final String putCalculationConfig = _fxCurveCalculationConfigs.get(putCurrencyString);
        if (putCalculationConfig == null) {
          throw new OpenGammaRuntimeException("Could not get curve calculation config for " + putCurrencyString);
        }
        final String putDiscountingCurve = _fxDiscountingCurveNames.get(putCurrencyString);
        if (putDiscountingCurve == null) {
          throw new OpenGammaRuntimeException("Could not get discounting curve for " + putCurrencyString);
        }
        final String callCalculationConfig = _fxCurveCalculationConfigs.get(callCurrencyString);
        if (callCalculationConfig == null) {
          throw new OpenGammaRuntimeException("Could not get curve calculation config for " + callCurrencyString);
        }
        final String callDiscountingCurve = _fxDiscountingCurveNames.get(callCurrencyString);
        if (callDiscountingCurve == null) {
          throw new OpenGammaRuntimeException("Could not get discounting curve for " + callCurrencyString);
        }
        final ValueProperties deltaProperties = commonProperties.copy()
            .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD)
            .with(FXOptionBlackFunction.PUT_CURVE, putDiscountingCurve)
            .with(FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG, putCalculationConfig)
            .with(FXOptionBlackFunction.CALL_CURVE, callDiscountingCurve)
            .with(FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG, callCalculationConfig)
            .with(ValuePropertyNames.SURFACE, _fxVolSurfaceName)
            .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, _fxVolInterpolator)
            .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, _fxVolLeftExtrapolator)
            .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, _fxVolRightExtrapolator)
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.FX_CURRENCY_EXPOSURE).get();
        final ValueRequirement delta = new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, deltaProperties);
        return Sets.newHashSet(delta);
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueRequirement> visitBondFutureSecurity(final BondFutureSecurity security) {
        final String securityCurrency = FinancialSecurityUtils.getCurrency(security).getCode();
        final String calculationConfig = _curveCalculationConfigs.get(securityCurrency);
        if (calculationConfig == null) {
          throw new OpenGammaRuntimeException("Could not get curve calculation config for " + securityCurrency);
        }
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, calculationConfig)
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES).get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueRequirement> visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
        final String securityCurrency = FinancialSecurityUtils.getCurrency(security).getCode();
        final String calculationConfig = _irFuturesCurveCalculationConfigs.get(securityCurrency);
        if (calculationConfig == null) {
          throw new OpenGammaRuntimeException("Could not get curve calculation config for " + securityCurrency);
        }
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, calculationConfig)
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES).get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueRequirement> visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
        final String securityCurrency = FinancialSecurityUtils.getCurrency(security).getCode();
        final String calculationConfig = _curveCalculationConfigs.get(securityCurrency);
        if (calculationConfig == null) {
          throw new OpenGammaRuntimeException("Could not get curve calculation config for " + securityCurrency);
        }
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.SURFACE, _irFutureOptionVolSurfaceName)
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, calculationConfig)
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES).get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueRequirement> visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
        final String securityCurrency = FinancialSecurityUtils.getCurrency(security).getCode();
        final String calculationConfig = _curveCalculationConfigs.get(securityCurrency);
        if (calculationConfig == null) {
          throw new OpenGammaRuntimeException("Could not get curve calculation config for " + securityCurrency);
        }
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.SURFACE, _bondFutureOptionVolSurfaceName)
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, calculationConfig)
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES).get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      @Override
      public Set<ValueRequirement> visitEquitySecurity(final EquitySecurity security) {
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, "Delta").get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      @Override
      public Set<ValueRequirement> visitEquityOptionSecurity(final EquityOptionSecurity security) {
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, "Delta").get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      @Override
      public Set<ValueRequirement> visitCashSecurity(final CashSecurity security) {
        return createValueRequirementsForCashLikeSecurity(security.getCurrency());
      }

      @Override
      public Set<ValueRequirement> visitCashFlowSecurity(final CashFlowSecurity security) {
        return createValueRequirementsForCashLikeSecurity(security.getCurrency());
      }

      @Override
      public Set<ValueRequirement> visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.BUCKETED_CS01)
            .get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      @Override
      public Set<ValueRequirement> visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.BUCKETED_CS01)
            .get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      @Override
      public Set<ValueRequirement> visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.BUCKETED_CS01)
            .get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      @Override
      public Set<ValueRequirement> visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.BUCKETED_CS01)
            .get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      private Set<ValueRequirement> createValueRequirementsForCashLikeSecurity(final Currency currency) {
        final String config = _curveCalculationConfigs.get(currency);
        if (config == null) {
          throw new OpenGammaRuntimeException("Could not get curve calculation configuration for " + currency);
        }
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, config)
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES).get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

    };
  }

}
