/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.cashflow;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.PortfolioMapper;
import com.opengamma.core.position.impl.SecurityTypeMapperFunction;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.financial.analytics.cashflow.FixedPaymentMatrix;
import com.opengamma.financial.analytics.cashflow.FloatingPaymentMatrix;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class PaymentService {
  
  private static final Logger s_logger = LoggerFactory.getLogger(PaymentService.class);
  
  private static final String VIEW_DEFINITION_PREFIX = "Cash flows for ";
  private static final String CALC_CONFIG_NAME = "Default";
  
  private static final String[] CASH_FLOW_VALUE_NAMES = new String[] {
    ValueRequirementNames.FIXED_PAY_CASH_FLOWS,
    ValueRequirementNames.FLOATING_PAY_CASH_FLOWS,
    ValueRequirementNames.FIXED_RECEIVE_CASH_FLOWS,
    ValueRequirementNames.FLOATING_RECEIVE_CASH_FLOWS };
  
  private static final Map<String, PaymentType> PAYMENT_TYPE_MAP = ImmutableMap.of(
      ValueRequirementNames.FIXED_PAY_CASH_FLOWS, PaymentType.FIXED,
      ValueRequirementNames.FLOATING_PAY_CASH_FLOWS, PaymentType.FLOAT,
      ValueRequirementNames.FIXED_RECEIVE_CASH_FLOWS, PaymentType.FIXED,
      ValueRequirementNames.FLOATING_RECEIVE_CASH_FLOWS, PaymentType.FLOAT);
  
  private static final Map<String, PaymentDirection> PAYMENT_DIRECTION_MAP = ImmutableMap.of(
      ValueRequirementNames.FIXED_PAY_CASH_FLOWS, PaymentDirection.PAY,
      ValueRequirementNames.FLOATING_PAY_CASH_FLOWS, PaymentDirection.PAY,
      ValueRequirementNames.FIXED_RECEIVE_CASH_FLOWS, PaymentDirection.RECEIVE,
      ValueRequirementNames.FLOATING_RECEIVE_CASH_FLOWS, PaymentDirection.RECEIVE);
  
  private final ViewProcessor _viewProcessor;
  private final ConfigMaster _userConfigMaster;
  private final PositionSource _positionSource;
  private final SecuritySource _securitySource;
  
  public PaymentService(ViewProcessor viewProcessor, ConfigMaster userConfigMaster,
      PositionSource positionSource, SecuritySource securitySource) {
    _viewProcessor = viewProcessor;
    _userConfigMaster = userConfigMaster;
    _positionSource = positionSource;
    _securitySource = securitySource;
  }

  //-------------------------------------------------------------------------
  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }

  public ConfigMaster getUserConfigMaster() {
    return _userConfigMaster;
  }
  
  public PositionSource getPositionSource() {
    return _positionSource;
  }
  
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  //-------------------------------------------------------------------------
  public PortfolioPaymentDiary getPortfolioPaymentDiary(UniqueId portfolioId) {
    Portfolio portfolio = getPositionSource().getPortfolio(portfolioId);
    portfolio = PortfolioCompiler.resolvePortfolio(portfolio, Executors.newSingleThreadExecutor(), getSecuritySource());
    UniqueId viewDefinitionId = getPaymentViewDefinition(portfolio);
    
    ViewClient viewClient = getViewProcessor().createViewClient(UserPrincipal.getTestUser());
    viewClient.attachToViewProcess(viewDefinitionId, ExecutionOptions.singleCycle(Instant.now(), MarketData.live()));
    ViewComputationResultModel result;
    try {
      viewClient.waitForCompletion();
      result = viewClient.getLatestResult();
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted while waiting for payment diary calculations");
    } finally {
      viewClient.shutdown();
    }
    if (result == null) {
      throw new OpenGammaRuntimeException("Cash flow view failed to run");
    }
    PortfolioPaymentDiary paymentDiary = new PortfolioPaymentDiary();
    ViewCalculationResultModel calcResults = result.getCalculationResult(CALC_CONFIG_NAME);
    if (calcResults == null) {
      throw new OpenGammaRuntimeException("No payments were calculated");
    }
    for (ComputationTargetSpecification targetSpec : calcResults.getAllTargets()) {
      if (targetSpec.getType() != ComputationTargetType.POSITION) {
        continue;
      }
      UniqueId positionId = targetSpec.getUniqueId();
      Position position = getPositionSource().getPosition(positionId);
      position.getSecurityLink().resolve(getSecuritySource());
      for (ComputedValueResult targetResult : calcResults.getValues(targetSpec).values()) {
        String valueName = targetResult.getSpecification().getValueName();
        PaymentType paymentType = getPaymentType(valueName);
        PaymentDirection direction = getPaymentDirection(valueName);
        switch (paymentType) {
          case FIXED:
            if (!(targetResult.getValue() instanceof FixedPaymentMatrix)) {
              s_logger.error("Skipping result with unexpected type: " + targetResult);
              continue;
            }
            addFixedPayments((FixedPaymentMatrix) targetResult.getValue(), direction, position, paymentDiary);
            break;
          case FLOAT:
            if (!(targetResult.getValue() instanceof FloatingPaymentMatrix)) {
              s_logger.error("Skipping result with unexpected type: " + targetResult);
              continue;
            }
            addFloatingPayments((FloatingPaymentMatrix) targetResult.getValue(), direction, position, paymentDiary);
            break;
        }
      }
    }
    return paymentDiary;
  }
  
  private void addFixedPayments(FixedPaymentMatrix paymentMatrix, PaymentDirection direction, Position position, PortfolioPaymentDiary paymentDiary) {
    for (Map.Entry<LocalDate, MultipleCurrencyAmount> paymentEntry : paymentMatrix.getValues().entrySet()) {
      LocalDate date = paymentEntry.getKey();
      MultipleCurrencyAmount multipleCurrencyAmount = paymentEntry.getValue();
      for (CurrencyAmount currencyAmount : multipleCurrencyAmount.getCurrencyAmounts()) {
        paymentDiary.add(date, new PositionPayment(position, PaymentType.FIXED, direction, null, currencyAmount));
      }
    }
  }
  
  private void addFloatingPayments(FloatingPaymentMatrix paymentMatrix, PaymentDirection direction, Position position, PortfolioPaymentDiary paymentDiary) {
    for (Map.Entry<LocalDate, List<Pair<CurrencyAmount, String>>> paymentEntry : paymentMatrix.getValues().entrySet()) {
      LocalDate date = paymentEntry.getKey();
      for (Pair<CurrencyAmount, String> payment : paymentEntry.getValue()) {
        CurrencyAmount amount = payment.getFirst();
        String index = payment.getSecond();
        paymentDiary.add(date, new PositionPayment(position, PaymentType.FLOAT, direction, index, amount));
      }
    }
  }
  
  private UniqueId getPaymentViewDefinition(Portfolio portfolio) {
    ViewDefinition viewDefinition = new ViewDefinition(VIEW_DEFINITION_PREFIX + portfolio.getName(), portfolio.getUniqueId(), UserPrincipal.getTestUser());
    ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, CALC_CONFIG_NAME);
    for (String requirementName : CASH_FLOW_VALUE_NAMES) {
      for (String securityType : getPortfolioSecurityTypes(portfolio)) {
        calcConfig.addPortfolioRequirement(securityType, requirementName, ValueProperties.none());
      }
    }
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    ConfigItem<ViewDefinition> configItem = ConfigItem.of(viewDefinition, viewDefinition.getName());
    ConfigDocument configDoc = getUserConfigMaster().add(new ConfigDocument(configItem));
    return configDoc.getUniqueId();
  }
  
  private Set<String> getPortfolioSecurityTypes(Portfolio portfolio) {
    return PortfolioMapper.mapToSet(portfolio.getRootNode(), new SecurityTypeMapperFunction());
  }
  
  private PaymentType getPaymentType(String valueName) {
    PaymentType type = PAYMENT_TYPE_MAP.get(valueName);
    if (type == null) {
      throw new OpenGammaRuntimeException("Unknown cash-flow value name '" + valueName + "'");
    }
    return type;
  }
  
  private PaymentDirection getPaymentDirection(String valueName) {
    PaymentDirection direction = PAYMENT_DIRECTION_MAP.get(valueName);
    if (direction == null) {
      throw new OpenGammaRuntimeException("Unknown cash-flow value name '" + valueName + "'");
    }
    return direction;
  }
  
}
