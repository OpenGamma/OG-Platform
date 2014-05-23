/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.DepthFirstPortfolioNodeTraverser;
import com.opengamma.core.position.impl.PortfolioNodeTraversalCallback;
import com.opengamma.core.position.impl.PortfolioNodeTraverser;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.calcnode.MissingValue;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Class to analyze view processor result sets and return statistics about available results.
 */
public class ViewProcessStatsProcessor {
  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcessStatsProcessor.class);
  
  private CompiledViewDefinition _compiledViewDef;
  private ViewComputationResultModel _viewComputationResultModel;
  private int _successes;
  private int _failures;
  private int _errors;
  private int _total;

  public ViewProcessStatsProcessor(CompiledViewDefinition compiledViewDef, ViewComputationResultModel viewComputationResultModel) {
    _compiledViewDef = compiledViewDef;
    _viewComputationResultModel = viewComputationResultModel;
  }
  
  public void processResult() {

    ViewDefinition viewDefinition = _compiledViewDef.getViewDefinition();
    for (final String calcConfigName : viewDefinition.getAllCalculationConfigurationNames()) {
      ViewCalculationConfiguration calcConfig = viewDefinition.getCalculationConfiguration(calcConfigName);
      final ValueMappings valueMappings = new ValueMappings(_compiledViewDef);
      final ViewCalculationResultModel calculationResult = _viewComputationResultModel.getCalculationResult(calcConfigName);
      final Map<String, Set<Pair<String, ValueProperties>>> portfolioRequirementsBySecurityType = calcConfig.getPortfolioRequirementsBySecurityType();
      Portfolio portfolio = _compiledViewDef.getPortfolio();
      PortfolioNodeTraverser traverser = new DepthFirstPortfolioNodeTraverser(new PortfolioNodeTraversalCallback() {

        @Override
        public void preOrderOperation(PortfolioNode parentNode, Position position) {
          UniqueId positionId = position.getUniqueId().toLatest();
          // then construct a chained target spec pointing at a specific position.
          ComputationTargetSpecification breadcrumbTargetSpec = ComputationTargetSpecification.of(parentNode).containing(ComputationTargetType.POSITION, positionId);
          ComputationTargetSpecification targetSpec = ComputationTargetSpecification.of(position);
          Map<Pair<String, ValueProperties>, ComputedValueResult> values = calculationResult.getValues(targetSpec);
          String securityType = position.getSecurity().getSecurityType();
          Set<Pair<String, ValueProperties>> valueRequirements = portfolioRequirementsBySecurityType.get(securityType);
          s_logger.info("Processing valueRequirement " + valueRequirements + " for security type " + securityType);
          if (valueRequirements != null) {
            for (Pair<String, ValueProperties> valueRequirement : valueRequirements) {
              ValueRequirement valueReq = new ValueRequirement(valueRequirement.getFirst(), breadcrumbTargetSpec, valueRequirement.getSecond());
              ValueSpecification valueSpec = valueMappings.getValueSpecification(calcConfigName, valueReq);
              if (valueSpec == null) {
                s_logger.debug("Couldn't get reverse value spec mapping from requirement: " + valueReq.toString());
                _failures++;
              } else {
                Pair<String, ValueProperties> valueKey = Pairs.of(valueSpec.getValueName(), valueSpec.getProperties());
                ComputedValueResult computedValueResult = values != null ? values.get(valueKey) : null;
                if (computedValueResult != null) {
                  if (computedValueResult.getValue() instanceof MissingValue) {
                    _errors++;
                  } else {
                    _successes++;
                  }
                } else {
                  _failures++;
                }
              }
              _total++;
            }
          }
        }

        @Override
        public void preOrderOperation(PortfolioNode portfolioNode) {}

        @Override
        public void postOrderOperation(PortfolioNode parentNode, Position position) {}

        @Override
        public void postOrderOperation(PortfolioNode portfolioNode) {}

      });
      traverser.traverse(portfolio.getRootNode());
    }
  }


  
  public int getSuccesses() {
    return _successes;
  }
  
  public int getFailures() {
    return _failures;
  }

  public int getErrors() {
    return _errors;
  }

  public int getTotal() {
    return _total;
  }

  public double getSuccessPercentage() {
    return 100d * _successes / _total;
  }

}
