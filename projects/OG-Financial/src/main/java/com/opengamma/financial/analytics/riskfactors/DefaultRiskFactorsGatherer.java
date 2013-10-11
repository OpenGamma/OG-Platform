/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.riskfactors;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.AbstractPortfolioNodeTraversalCallback;
import com.opengamma.core.position.impl.PortfolioNodeTraverser;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.financial.analytics.MissingInputsFunction;
import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Default implementation of {@link RiskFactorsGatherer}.
 */
public class DefaultRiskFactorsGatherer extends FinancialSecurityVisitorAdapter<Set<Pair<String, ValueProperties>>> implements RiskFactorsGatherer {
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultRiskFactorsGatherer.class);
  private final SecuritySource _securities;
  private final RiskFactorsConfigurationProvider _configProvider;

  public DefaultRiskFactorsGatherer(final SecuritySource securities, final RiskFactorsConfigurationProvider configProvider) {
    ArgumentChecker.notNull(securities, "securities");
    ArgumentChecker.notNull(configProvider, "configProvider");
    _securities = securities;
    _configProvider = configProvider;
  }

  @Override
  public Set<ValueRequirement> getPositionRiskFactors(final Position position) {
    ArgumentChecker.notNull(position, "position");
    final Set<Pair<String, ValueProperties>> securityRiskFactors = ((FinancialSecurity) position.getSecurity()).accept(this);
    if (securityRiskFactors.isEmpty()) {
      return ImmutableSet.of();
    }
    final Set<ValueRequirement> results = new HashSet<ValueRequirement>(securityRiskFactors.size());
    for (final Pair<String, ValueProperties> securityRiskFactor : securityRiskFactors) {
      results.add(getValueRequirement(position, securityRiskFactor.getFirst(), securityRiskFactor.getSecond()));
    }
    return results;
  }

  @Override
  public Set<ValueRequirement> getPositionRiskFactors(final Portfolio portfolio) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    final RiskFactorPortfolioTraverser callback = new RiskFactorPortfolioTraverser();
    callback.traverse(portfolio.getRootNode());
    return callback.getRiskFactors();
  }

  @Override
  public void addPortfolioRiskFactors(final Portfolio portfolio, final ViewCalculationConfiguration calcConfig) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    ArgumentChecker.notNull(calcConfig, "calcConfig");
    final RiskFactorPortfolioTraverser callback = new RiskFactorPortfolioTraverser(calcConfig);
    callback.traverse(portfolio.getRootNode());
  }

  //-------------------------------------------------------------------------
  private class RiskFactorPortfolioTraverser extends AbstractPortfolioNodeTraversalCallback {

    private final ViewCalculationConfiguration _calcConfig;
    private final Set<ValueRequirement> _valueRequirements = new HashSet<ValueRequirement>();

    public RiskFactorPortfolioTraverser() {
      this(null);
    }

    public RiskFactorPortfolioTraverser(final ViewCalculationConfiguration calcConfig) {
      _calcConfig = calcConfig;
    }

    public void traverse(final PortfolioNode rootNode) {
      PortfolioNodeTraverser.depthFirst(this).traverse(rootNode);
    }

    public Set<ValueRequirement> getRiskFactors() {
      return new HashSet<ValueRequirement>(_valueRequirements);
    }

    @Override
    public void preOrderOperation(final PortfolioNode portfolioNode) {
    }

    @Override
    public void preOrderOperation(final PortfolioNode parentNode, final Position position) {
      // REVIEW 2012-09-17 Andrew -- [PLAT-2286] Should we check whether the position has already been visited?
      final Set<ValueRequirement> riskFactorRequirements = DefaultRiskFactorsGatherer.this.getPositionRiskFactors(position);
      _valueRequirements.addAll(riskFactorRequirements);
      if (_calcConfig != null) {
        for (final ValueRequirement riskFactorRequirement : riskFactorRequirements) {
          _calcConfig.addPortfolioRequirement(position.getSecurity().getSecurityType(),
            riskFactorRequirement.getValueName(), riskFactorRequirement.getConstraints());
        }
      }
    }

    @Override
    public void postOrderOperation(final PortfolioNode parentNode, final Position position) {
    }

    @Override
    public void postOrderOperation(final PortfolioNode portfolioNode) {
    }

  }

  //-------------------------------------------------------------------------
  // Securities

  @Override
  public Set<Pair<String, ValueProperties>> visitCorporateBondSecurity(final CorporateBondSecurity security) {
    return ImmutableSet.of(
      getYieldCurveNodeSensitivities(getFundingCurve(), security.getCurrency()),
      getPresentValue(ValueProperties.builder()),
      getPV01(getFundingCurve()));
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
    return ImmutableSet.of(
      getYieldCurveNodeSensitivities(getFundingCurve(), security.getCurrency()),
      getPresentValue(ValueProperties.builder()),
      getPV01(getFundingCurve()));
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
    return ImmutableSet.of(
      getYieldCurveNodeSensitivities(getFundingCurve(), security.getCurrency()),
      getPresentValue(ValueProperties.builder()),
      getPV01(getFundingCurve()));
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitCashSecurity(final CashSecurity security) {
    return ImmutableSet.of(
      getYieldCurveNodeSensitivities(getFundingCurve(), security.getCurrency()),
      getYieldCurveNodeSensitivities(getForwardCurve(security.getCurrency()), security.getCurrency()),
      getPresentValue(ValueProperties.builder()),
      getPV01(getFundingCurve()),
      getPV01(getForwardCurve(security.getCurrency())));
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitEquitySecurity(final EquitySecurity security) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitFRASecurity(final FRASecurity security) {
    return ImmutableSet.of(
      getYieldCurveNodeSensitivities(getFundingCurve(), security.getCurrency()),
      getYieldCurveNodeSensitivities(getForwardCurve(security.getCurrency()), security.getCurrency()),
      getPresentValue(ValueProperties.builder()),
      getPV01(getFundingCurve()),
      getPV01(getForwardCurve(security.getCurrency())));
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitSwapSecurity(final SwapSecurity security) {
    final ImmutableSet.Builder<Pair<String, ValueProperties>> builder = ImmutableSet.<Pair<String, ValueProperties>>builder();

    // At the moment pay and receive must be the same currency, so any one of them is sufficient
    final Notional payNotional = security.getPayLeg().getNotional();
    final Notional receiveNotional = security.getReceiveLeg().getNotional();
    if (payNotional instanceof InterestRateNotional && receiveNotional instanceof InterestRateNotional) {
      final Currency ccy = ((InterestRateNotional) payNotional).getCurrency();
      builder.add(getYieldCurveNodeSensitivities(getFundingCurve(), ccy));
      builder.add(getYieldCurveNodeSensitivities(getForwardCurve(ccy), ccy));
      final InterestRateInstrumentType type = SwapSecurityUtils.getSwapType(security);
      if (type == InterestRateInstrumentType.SWAP_CMS_CMS ||
        type == InterestRateInstrumentType.SWAP_FIXED_CMS ||
        type == InterestRateInstrumentType.SWAP_IBOR_CMS) {
        builder.add(getVegaQuoteCubeMatrix(ValueProperties.with(ValuePropertyNames.CUBE, "BLOOMBERG")));
      } else if (type == InterestRateInstrumentType.SWAP_FIXED_IBOR ||
        type == InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD ||
        type == InterestRateInstrumentType.SWAP_IBOR_IBOR) {
        builder.add(getPV01(getFundingCurve()));
        builder.add(getPV01(getForwardCurve(ccy)));
      }
    }

    builder.add(getPresentValue(ValueProperties.builder()));
    return builder.build();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    final ExternalId underlyingId = security.getUnderlyingId();
    return ImmutableSet.<Pair<String, ValueProperties>>builder()
      .add(getEquityValue(ValueRequirementNames.PRESENT_VALUE, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VEGA_QUOTE_MATRIX, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VEGA_MATRIX, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VALUE_VEGA, underlyingId))
      .add(getEquityValue(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VALUE_DELTA, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VALUE_GAMMA, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VALUE_VOMMA, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VALUE_VANNA, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VALUE_RHO, underlyingId))
      .build();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitEquityOptionSecurity(final EquityOptionSecurity security) {
    final ExternalId underlyingId = security.getUnderlyingId();
    return ImmutableSet.<Pair<String, ValueProperties>>builder()
      .add(getEquityValue(ValueRequirementNames.PRESENT_VALUE, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VEGA_QUOTE_MATRIX, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VEGA_MATRIX, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VALUE_VEGA, underlyingId))
      .add(getEquityValue(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VALUE_DELTA, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VALUE_GAMMA, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VALUE_VOMMA, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VALUE_VANNA, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VALUE_RHO, underlyingId))
      .build();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
    final ExternalId underlyingId = security.getUnderlyingId();
    return ImmutableSet.<Pair<String, ValueProperties>>builder()
      .add(getEquityValue(ValueRequirementNames.PRESENT_VALUE, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VEGA_QUOTE_MATRIX, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VALUE_VEGA, underlyingId))
      .add(getEquityValue(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VALUE_DELTA, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VALUE_GAMMA, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VALUE_VOMMA, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VALUE_VANNA, underlyingId))
      .add(getEquityValue(ValueRequirementNames.VALUE_RHO, underlyingId))
      .build();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitFXOptionSecurity(final FXOptionSecurity security) {
    final ImmutableSet.Builder<Pair<String, ValueProperties>> builder = ImmutableSet.<Pair<String, ValueProperties>>builder()
        .add(getFXPresentValueWithCalculationMethod())
        .add(getFXCurrencyExposureWithCalculationMethod())
        .add(getFXYieldCurveNodeSensitivities(_configProvider.getFXCurve(security.getCallCurrency()), security.getCallCurrency()))
        .add(getFXYieldCurveNodeSensitivities(_configProvider.getFXCurve(security.getPutCurrency()), security.getPutCurrency()));
    final String surfaceName = _configProvider.getFXVanillaOptionSurfaceName(security.getCallCurrency(), security.getPutCurrency());
    if (surfaceName != null) {
      builder.add(getFXVegaQuoteMatrix(ValueProperties
          .with(ValuePropertyNames.SURFACE, surfaceName)
          .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX)));
      builder.add(getFXVegaMatrix(ValueProperties
          .with(ValuePropertyNames.SURFACE, surfaceName)));
    } else {
      s_logger.warn("Could not build surface name for {}", security);
    }
    return builder.build();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
    final ImmutableSet.Builder<Pair<String, ValueProperties>> builder = ImmutableSet.<Pair<String, ValueProperties>>builder()
        .add(getFXPresentValueWithCalculationMethod())
        .add(getFXCurrencyExposureWithCalculationMethod())
        .add(getFXYieldCurveNodeSensitivities(_configProvider.getFXCurve(security.getCallCurrency()), security.getCallCurrency()))
        .add(getFXYieldCurveNodeSensitivities(_configProvider.getFXCurve(security.getPutCurrency()), security.getPutCurrency()));
    final String surfaceName = _configProvider.getFXVanillaOptionSurfaceName(security.getCallCurrency(), security.getPutCurrency());
    if (surfaceName != null) {
      builder.add(getFXVegaQuoteMatrix(ValueProperties
          .with(ValuePropertyNames.SURFACE, surfaceName)
          .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX)));
      builder.add(getFXVegaMatrix(ValueProperties
          .with(ValuePropertyNames.SURFACE, surfaceName)));
    } else {
      s_logger.warn("Could not build surface name for {}", security);
    }
    return builder.build();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
    final ImmutableSet.Builder<Pair<String, ValueProperties>> builder = ImmutableSet.<Pair<String, ValueProperties>>builder()
        .add(getFXPresentValueWithCalculationMethod())
        .add(getFXCurrencyExposureWithCalculationMethod())
        .add(getFXYieldCurveNodeSensitivities(_configProvider.getFXCurve(security.getCallCurrency()), security.getCallCurrency()))
        .add(getFXYieldCurveNodeSensitivities(_configProvider.getFXCurve(security.getPutCurrency()), security.getPutCurrency()));
    final String surfaceName = _configProvider.getFXVanillaOptionSurfaceName(security.getCallCurrency(), security.getPutCurrency());
    if (surfaceName != null) {
      builder.add(getFXVegaQuoteMatrix(ValueProperties
          .with(ValuePropertyNames.SURFACE, surfaceName)
          .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX)));
      builder.add(getFXVegaMatrix(ValueProperties
          .with(ValuePropertyNames.SURFACE, surfaceName)));
    } else {
      s_logger.warn("Could not build surface name for {}", security);
    }
    return builder.build();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitSwaptionSecurity(final SwaptionSecurity security) {
    return ImmutableSet.<Pair<String, ValueProperties>>builder()
      .add(getYieldCurveNodeSensitivities(getFundingCurve(), security.getCurrency()))
      .add(getYieldCurveNodeSensitivities(getForwardCurve(security.getCurrency()), security.getCurrency()))
      .addAll(getSabrSensitivities())
      .add(getPresentValue(ValueProperties.with(ValuePropertyNames.CUBE, _configProvider.getSwaptionVolatilityCubeName(security.getCurrency()))))
      .add(getVegaQuoteCubeMatrix(ValueProperties.with(ValuePropertyNames.CUBE, "BLOOMBERG"))).build();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    final Currency ccy = security.getCurrency();
    final String tickerStr = security.getExternalIdBundle().getValue(ExternalSchemes.BLOOMBERG_TICKER);
    final String futurePrefix = tickerStr.substring(0, 2);
    final String surfaceName = _configProvider.getIRFutureOptionVolatilitySurfaceName(futurePrefix);
    return ImmutableSet.<Pair<String, ValueProperties>>builder()
      .addAll(getSabrSensitivities())
      .add(getYieldCurveNodeSensitivities(getFundingCurve(), ccy))
      .add(getYieldCurveNodeSensitivities(getForwardCurve(ccy), ccy))
      .add(getPresentValue(ValueProperties.with(ValuePropertyNames.SURFACE, surfaceName)
                                          .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION)))
      .add(getVegaQuoteMatrix(ValueProperties
        .with(ValuePropertyNames.SURFACE, surfaceName)
          .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION))).build();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity commodityFutureOptionSecurity) {
    s_logger.warn("Commodity Future Option risk factors not implemented");
    return Collections.emptySet();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
    s_logger.warn("FX Future Option risk factors not implemented");
    return Collections.emptySet();  }

  @Override
  public Set<Pair<String, ValueProperties>> visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity equityIndexDividendFutureOptionSecurity) {
    s_logger.warn("Equity index dividend future option risk factors not implemented");
    return Collections.emptySet();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
    final ImmutableSet.Builder<Pair<String, ValueProperties>> builder = ImmutableSet.<Pair<String, ValueProperties>>builder()
        .add(getFXPresentValueWithCalculationMethod())
        .add(getFXCurrencyExposureWithCalculationMethod())
        .add(getFXYieldCurveNodeSensitivities(_configProvider.getFXCurve(security.getCallCurrency()), security.getCallCurrency()))
        .add(getFXYieldCurveNodeSensitivities(_configProvider.getFXCurve(security.getPutCurrency()), security.getPutCurrency()));
    final String surfaceName = _configProvider.getFXVanillaOptionSurfaceName(security.getCallCurrency(), security.getPutCurrency());
    if (surfaceName != null) {
      builder.add(getFXVegaQuoteMatrix(ValueProperties
          .with(ValuePropertyNames.SURFACE, surfaceName)
          .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX)));
      builder.add(getFXVegaMatrix(ValueProperties
          .with(ValuePropertyNames.SURFACE, surfaceName)));
    } else {
      s_logger.warn("Could not build surface name for {}", security);
    }
    return builder.build();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
    final ImmutableSet.Builder<Pair<String, ValueProperties>> builder = ImmutableSet.<Pair<String, ValueProperties>>builder()
        .add(getFXPresentValueWithCalculationMethod())
        .add(getFXCurrencyExposureWithCalculationMethod())
        .add(getFXYieldCurveNodeSensitivities(_configProvider.getFXCurve(security.getCallCurrency()), security.getCallCurrency()))
        .add(getFXYieldCurveNodeSensitivities(_configProvider.getFXCurve(security.getPutCurrency()), security.getPutCurrency()));
    final String surfaceName = _configProvider.getFXVanillaOptionSurfaceName(security.getCallCurrency(), security.getPutCurrency());
    if (surfaceName != null) {
      builder.add(getFXVegaQuoteMatrix(ValueProperties
          .with(ValuePropertyNames.SURFACE, surfaceName)
          .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX)));
      builder.add(getFXVegaMatrix(ValueProperties
          .with(ValuePropertyNames.SURFACE, surfaceName)));
    } else {
      s_logger.warn("Could not build surface name for {}", security);
    }
    return builder.build();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitFXForwardSecurity(final FXForwardSecurity security) {
    return ImmutableSet.of(
      getFXPresentValue(),
      getFXCurrencyExposure(),
      getYieldCurveNodeSensitivities(_configProvider.getFXCurve(security.getPayCurrency()), security.getPayCurrency()),
      getYieldCurveNodeSensitivities(_configProvider.getFXCurve(security.getReceiveCurrency()), security.getReceiveCurrency()));
  }

  // REVIEW jim 23-Jan-2012 -- bit of a leap to copy fx forwards, but there you go.
  @Override
  public Set<Pair<String, ValueProperties>> visitNonDeliverableFXForwardSecurity(
    final NonDeliverableFXForwardSecurity security) {
    return ImmutableSet.of(
      getFXPresentValue(),
      getFXCurrencyExposure(),
      getYieldCurveNodeSensitivities(_configProvider.getFXCurve(security.getPayCurrency()), security.getPayCurrency()),
      getYieldCurveNodeSensitivities(_configProvider.getFXCurve(security.getReceiveCurrency()), security.getReceiveCurrency()));
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitCapFloorSecurity(final CapFloorSecurity security) {
    final String cubeName = _configProvider.getSwaptionVolatilityCubeName(security.getCurrency());
    return ImmutableSet.<Pair<String, ValueProperties>>builder()
      .add(getYieldCurveNodeSensitivities(getFundingCurve(), security.getCurrency()))
      .add(getYieldCurveNodeSensitivities(getForwardCurve(security.getCurrency()), security.getCurrency()))
      .addAll(getSabrSensitivities())
      .add(getVegaQuoteCubeMatrix(ValueProperties.with(ValuePropertyNames.CUBE, cubeName)))
      .add(getPresentValue(ValueProperties.with(ValuePropertyNames.CUBE, cubeName))).build();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
    final String cubeName = _configProvider.getSwaptionVolatilityCubeName(security.getCurrency());
    return ImmutableSet.<Pair<String, ValueProperties>>builder()
      .add(getYieldCurveNodeSensitivities(getFundingCurve(), security.getCurrency()))
      .add(getYieldCurveNodeSensitivities(getForwardCurve(security.getCurrency()), security.getCurrency()))
      .addAll(getSabrSensitivities())
      .add(getVegaQuoteCubeMatrix(ValueProperties.with(ValuePropertyNames.CUBE, cubeName)))
      .add(getPresentValue(ValueProperties.with(ValuePropertyNames.CUBE, cubeName))).build();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
    final String ticker = security.getSpotUnderlyingId().getValue();
    if (ticker != null) {
      return ImmutableSet.<Pair<String, ValueProperties>>builder()
        .add(getPresentValue(ValueProperties.builder()))
        .add(getYieldCurveNodeSensitivities(getFundingCurve(), security.getCurrency()))
        .add(getVegaQuoteMatrix(ValueProperties
          .with(ValuePropertyNames.SURFACE, "DEFAULT")
            .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, "EQUITY_OPTION"))).build();
    }
    s_logger.warn("Could not get underlying ticker for equity variance swap security, so excluding surface");
    return ImmutableSet.<Pair<String, ValueProperties>>builder()
        .add(getPresentValue(ValueProperties.builder()))
        .add(getYieldCurveNodeSensitivities(getFundingCurve(), security.getCurrency())).build();
  }

  //-------------------------------------------------------------------------
  // Futures

  @Override
  public Set<Pair<String, ValueProperties>> visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitBondFutureSecurity(final BondFutureSecurity security) {
    return ImmutableSet.of(
        getYieldCurveNodeSensitivities(getFundingCurve(), security.getCurrency()),
        getPresentValue(ValueProperties.builder()),
        getPV01(getFundingCurve()));
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitFXFutureSecurity(final FXFutureSecurity security) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitIndexFutureSecurity(final IndexFutureSecurity security) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    return ImmutableSet.of(
      getYieldCurveNodeSensitivities(getFundingCurve(), security.getCurrency()),
      getYieldCurveNodeSensitivities(getForwardCurve(security.getCurrency()), security.getCurrency()),
      getPresentValue(ValueProperties.builder()),
      getPV01(getFundingCurve()),
      getPV01(getForwardCurve(security.getCurrency())));
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitMetalFutureSecurity(final MetalFutureSecurity security) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitStockFutureSecurity(final StockFutureSecurity security) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitEquityFutureSecurity(final EquityFutureSecurity security) {
    return ImmutableSet.of(
      getYieldCurveNodeSensitivities(getFundingCurve(), security.getCurrency()),
      getPresentValue(ValueProperties.builder()));
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    return ImmutableSet.of(
      getYieldCurveNodeSensitivities(getFundingCurve(), security.getCurrency()),
      getPresentValue(ValueProperties.builder()));
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
    throw new OpenGammaRuntimeException("Simple zero deposit security not supported");
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
    throw new OpenGammaRuntimeException("Periodic zero deposit security not supported");
  }

  @Override
  public Set<Pair<String, ValueProperties>> visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
    throw new OpenGammaRuntimeException("Continuous zero deposit security not supported");
  }

  //-------------------------------------------------------------------------
  private Pair<String, ValueProperties> getYieldCurveNodeSensitivities(final String curve, final Currency currency) {
    final ValueProperties.Builder constraints = ValueProperties
      .with(ValuePropertyNames.CURVE_CURRENCY, currency.getCode())
      .with(ValuePropertyNames.CURVE, curve);
    return getRiskFactor(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, constraints);
  }

  private Pair<String, ValueProperties> getFXYieldCurveNodeSensitivities(final String curve, final Currency currency) {
    final ValueProperties.Builder constraints = ValueProperties
      .with(ValuePropertyNames.CURVE_CURRENCY, currency.getCode())
      .with(ValuePropertyNames.CURVE, curve)
      .with(ValuePropertyNames.CALCULATION_METHOD, _configProvider.getFXCalculationMethod());
    return getRiskFactor(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, constraints);
  }

  //FIXME properties aren't correct
  private Pair<String, ValueProperties> getEquityValue(final String requirementName, final ExternalId underlying) {
    final com.opengamma.engine.value.ValueProperties.Builder builder = ValueProperties.builder().with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, _configProvider.getEquityFundingCurve())
                             .with(ValuePropertyNames.SURFACE, _configProvider.getEquityIndexOptionVolatilitySurfaceName(underlying.getValue()))
                             .with(ValuePropertyNames.CALCULATION_METHOD, _configProvider.getEquityCalculationMethod())
                             .with(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR, _configProvider.getEquitySmileInterpolator());
    return getRiskFactor(requirementName, builder);
  }


  private Pair<String, ValueProperties> getPresentValue(final ValueProperties.Builder constraints) {
    return getRiskFactor(ValueRequirementNames.PRESENT_VALUE, constraints);
  }

  private Pair<String, ValueProperties> getFXPresentValue() {
    return getFXPresentValue(ValueProperties.builder());
  }

  private Pair<String, ValueProperties> getFXPresentValueWithCalculationMethod() {
    return getFXPresentValue(ValueProperties.with(ValuePropertyNames.CALCULATION_METHOD, _configProvider.getFXCalculationMethod()));
  }

  private Pair<String, ValueProperties> getFXPresentValue(final ValueProperties.Builder constraints) {
    return getRiskFactor(ValueRequirementNames.FX_PRESENT_VALUE, constraints);
  }

  private Pair<String, ValueProperties> getFXCurrencyExposure() {
    return getFXCurrencyExposure(ValueProperties.builder());
  }

  private Pair<String, ValueProperties> getFXCurrencyExposureWithCalculationMethod() {
    return getFXCurrencyExposure(ValueProperties.with(ValuePropertyNames.CALCULATION_METHOD, _configProvider.getFXCalculationMethod()));
  }

  private Pair<String, ValueProperties> getFXCurrencyExposure(final ValueProperties.Builder constraints) {
    return getRiskFactor(ValueRequirementNames.FX_CURRENCY_EXPOSURE, constraints, false);
  }

  private Pair<String, ValueProperties> getVegaMatrix(final ValueProperties.Builder constraints) {
    return getRiskFactor(ValueRequirementNames.VEGA_MATRIX, constraints, false);
  }

  private Pair<String, ValueProperties> getFXVegaMatrix(final ValueProperties.Builder constraints) {
    return getRiskFactor(ValueRequirementNames.VEGA_MATRIX, constraints.with(ValuePropertyNames.CALCULATION_METHOD, _configProvider.getFXCalculationMethod()), false);
  }

  private Pair<String, ValueProperties> getVegaQuoteMatrix(final ValueProperties.Builder constraints) {
    return getRiskFactor(ValueRequirementNames.VEGA_QUOTE_MATRIX, constraints, false);
  }

  private Pair<String, ValueProperties> getFXVegaQuoteMatrix(final ValueProperties.Builder constraints) {
    return getRiskFactor(ValueRequirementNames.VEGA_QUOTE_MATRIX, constraints.with(ValuePropertyNames.CALCULATION_METHOD, _configProvider.getFXCalculationMethod()), false);
  }

  private Pair<String, ValueProperties> getVegaQuoteCubeMatrix(final ValueProperties.Builder constraints) {
    return getRiskFactor(ValueRequirementNames.VEGA_QUOTE_CUBE, constraints, false);
  }

  private Pair<String, ValueProperties> getValueDelta() {
    return getRiskFactor(ValueRequirementNames.VALUE_DELTA, ValueProperties.builder());
  }

  private Pair<String, ValueProperties> getPV01(final String curveName) {
    final ValueProperties.Builder constraints = ValueProperties
      .with(ValuePropertyNames.CURVE, curveName);
    return getRiskFactor(ValueRequirementNames.PV01, constraints, true);
  }

  private Pair<String, ValueProperties> getFXPV01(final String curveName) {
    final ValueProperties.Builder constraints = ValueProperties
      .with(ValuePropertyNames.CURVE, curveName);
    return getRiskFactor(ValueRequirementNames.PV01, constraints.with(ValuePropertyNames.CALCULATION_METHOD, _configProvider.getFXCalculationMethod()), true);
  }

  private Set<Pair<String, ValueProperties>> getSabrSensitivities() {
    return ImmutableSet.of(
      getPresentValueSabrAlphaSensitivity(),
      getPresentValueSabrRhoSensitivity(),
      getPresentValueSabrNuSensitivity());
  }

  private Pair<String, ValueProperties> getPresentValueSabrAlphaSensitivity() {
    return getRiskFactor(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY);
  }

  private Pair<String, ValueProperties> getPresentValueSabrRhoSensitivity() {
    return getRiskFactor(ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY);
  }

  private Pair<String, ValueProperties> getPresentValueSabrNuSensitivity() {
    return getRiskFactor(ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY);
  }

  //-------------------------------------------------------------------------
  private Pair<String, ValueProperties> getRiskFactor(final String valueName) {
    return getRiskFactor(valueName, ValueProperties.builder(), true);
  }

  private Pair<String, ValueProperties> getRiskFactor(final String valueName, final ValueProperties.Builder constraints) {
    return getRiskFactor(valueName, constraints, true);
  }

  private Pair<String, ValueProperties> getRiskFactor(final String valueName, final ValueProperties.Builder constraints, final boolean allowCurrencyOverride) {
    ArgumentChecker.notNull(valueName, "valueName");
    ArgumentChecker.notNull(constraints, "constraints");
    if (allowCurrencyOverride && getConfigProvider().getCurrencyOverride() != null) {
      constraints.with(ValuePropertyNames.CURRENCY, getConfigProvider().getCurrencyOverride().getCode());
    }
    constraints.with(ValuePropertyNames.AGGREGATION, MissingInputsFunction.AGGREGATION_STYLE_MISSING).withOptional(ValuePropertyNames.AGGREGATION);
    return Pairs.of(valueName, constraints.get());
  }

  private ValueRequirement getValueRequirement(final Position position, final String valueName, final ValueProperties constraints) {
    return new ValueRequirement(valueName, ComputationTargetSpecification.of(position), constraints);
  }

  //-------------------------------------------------------------------------
  private String getFundingCurve() {
    return getConfigProvider().getFundingCurve();
  }

  private String getForwardCurve(final Currency currency) {
    return getConfigProvider().getForwardCurve(currency);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unused")
  private SecuritySource getSecuritySource() {
    return _securities;
  }

  private RiskFactorsConfigurationProvider getConfigProvider() {
    return _configProvider;
  }


}
