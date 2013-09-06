/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import org.apache.commons.lang.mutable.MutableInt;
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
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Class to analyze view processor result sets and return statistics about available results.
 */
public class ViewProcessStatsProcessor {
  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcessStatsProcessor.class);
  
  private CompiledViewDefinition _compiledViewDef;
  private ViewComputationResultModel _viewComputationResultModel;
  private Map<ColumnRequirementBySecurityType, MutableInt> _successCountBySec;
  private Map<ColumnRequirementBySecurityType, MutableInt> _failureCountBySec;
  private Map<ColumnRequirementBySecurityType, MutableInt> _errorCountBySec;
  private Map<ColumnRequirementBySecurityType, MutableInt> _totalCountBySec;
  private Map<ColumnRequirement, MutableInt> _successCount;
  private Map<ColumnRequirement, MutableInt> _failureCount;
  private Map<ColumnRequirement, MutableInt> _errorCount;
  private Map<ColumnRequirement, MutableInt> _totalCount;
  private MutableInt _successes;
  private MutableInt _failures;
  private MutableInt _errors;
  private MutableInt _total;
  
  private TabularDataSupport _resultsBySecurityType;
  private TabularDataSupport _resultsByColumnRequirement;

  private CompositeType _columnReqRowType;
  private TabularType _columnReqTabularType;
  private CompositeType _columnReqBySecTypeRowType;
  private TabularType _columnReqBySecurityTypeTabularType;
  
  public ViewProcessStatsProcessor(CompiledViewDefinition compiledViewDef, ViewComputationResultModel viewComputationResultModel) {
    _compiledViewDef = compiledViewDef;
    _viewComputationResultModel = viewComputationResultModel;
    _successCountBySec = new HashMap<>();
    _failureCountBySec = new HashMap<>();
    _errorCountBySec = new HashMap<>();
    _totalCountBySec = new HashMap<>();
    _successCount = new HashMap<>();
    _failureCount = new HashMap<>();
    _errorCount = new HashMap<>();
    _totalCount = new HashMap<>();
    _successes = new MutableInt(0);
    _failures = new MutableInt(0);
    _errors = new MutableInt(0);
    _total = new MutableInt(0);
    
    _columnReqBySecTypeRowType = ColumnRequirementBySecurityType.getCompositeType();
    _columnReqBySecurityTypeTabularType = ColumnRequirementBySecurityType.getTablularType();
    
    _columnReqRowType = ColumnRequirement.getCompositeType();
    _columnReqTabularType = ColumnRequirement.getTabularType();
    
    _resultsBySecurityType = new TabularDataSupport(_columnReqBySecurityTypeTabularType);
    _resultsByColumnRequirement = new TabularDataSupport(_columnReqTabularType);
  }
  
  public void processResult() {
    try {
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
            s_logger.error("Processing valueRequirement " + valueRequirements + " for security type " + securityType);
            if (valueRequirements != null) {
              for (Pair<String, ValueProperties> valueRequirement : valueRequirements) {
                ColumnRequirementBySecurityType keyBySec = ColumnRequirementBySecurityType.of(securityType, ColumnRequirement.of(valueRequirement.getFirst(), valueRequirement.getSecond()));
                ValueRequirement valueReq = new ValueRequirement(valueRequirement.getFirst(), breadcrumbTargetSpec, valueRequirement.getSecond());
                ColumnRequirement key = ColumnRequirement.of(valueRequirement.getFirst(), valueRequirement.getSecond());
                ValueSpecification valueSpec = valueMappings.getValueSpecification(calcConfigName, valueReq);
                if (valueSpec == null) {
                  s_logger.error("Couldn't get reverse value spec mapping from requirement: " + valueReq.toString());
                  incCount(_failureCountBySec, keyBySec);
                  incCount(_failureCount, key);
                  _failures.increment();                  
                } else {
                  ObjectsPair<String, ValueProperties> valueKey = Pair.of(valueSpec.getValueName(), valueSpec.getProperties());
                  ComputedValueResult computedValueResult = values != null ? values.get(valueKey) : null;
                  if (computedValueResult != null) {
                    if (computedValueResult.getValue() instanceof MissingValue) {
                      incCount(_errorCountBySec, keyBySec);
                      incCount(_errorCount, key);
                      _errors.increment();
                    } else {
                      incCount(_successCountBySec, keyBySec);
                      incCount(_successCount, key);
                      _successes.increment();
                    }
                  } else {
                    incCount(_failureCountBySec, keyBySec);
                    incCount(_failureCount, key);
                    _failures.increment();
                  }
                }
                incCount(_totalCountBySec, keyBySec);
                incCount(_totalCount, key);
                _total.increment();
              }
            }
          }
          
          @Override
          public void preOrderOperation(PortfolioNode portfolioNode) {
          }
          
          @Override
          public void postOrderOperation(PortfolioNode parentNode, Position position) {
          }
          
          @Override
          public void postOrderOperation(PortfolioNode portfolioNode) {
          }
          
          private <T> void incCount(Map<T, MutableInt> countMap, 
                                T key) {
            if (!countMap.containsKey(key)) {
              countMap.put(key, new MutableInt(1));
            } else {
              countMap.get(key).increment();
            }          
          }
        });
        traverser.traverse(portfolio.getRootNode());        
      }
      convertToJMXComposites();    
    } catch (NullPointerException npe) {
      s_logger.error("NPE", npe);
    }
  }

  /**
   * convert nice objects into JMX rubbish
   */
  private void convertToJMXComposites() {
    _resultsBySecurityType.clear();
    _resultsByColumnRequirement.clear();
    // convert all the stats into table hopefully that JMX can parse without classloading
    for (Map.Entry<ColumnRequirementBySecurityType, MutableInt> entries : _totalCountBySec.entrySet()) {
      int total = entries.getValue().intValue();
      int success = 0;
      if (_successCountBySec.containsKey(entries.getKey())) {
        success = _successCountBySec.get(entries.getKey()).intValue();
      }
      int failures = 0;
      if (_failureCountBySec.containsKey(entries.getKey())) {
        failures = _failureCountBySec.get(entries.getKey()).intValue();
      }
      int errors = 0;
      if (_errorCountBySec.containsKey(entries.getKey())) {
        errors = _errorCountBySec.get(entries.getKey()).intValue();
      }
      _resultsBySecurityType.put(entries.getKey().toCompositeData(_columnReqBySecTypeRowType, success, failures, errors, total));
      s_logger.error("Adding entry to bySecurityType");
    }
    for (Map.Entry<ColumnRequirement, MutableInt> entries : _totalCount.entrySet()) {
      int total = entries.getValue().intValue();
      int success = 0;
      if (_successCount.containsKey(entries.getKey())) {
        success = _successCount.get(entries.getKey()).intValue();
      }
      int failures = 0;
      if (_failureCount.containsKey(entries.getKey())) {
        failures = _failureCount.get(entries.getKey()).intValue();
      }
      int errors = 0;
      if (_errorCount.containsKey(entries.getKey())) {
        errors = _errorCount.get(entries.getKey()).intValue();
      }
      _resultsByColumnRequirement.put(entries.getKey().toCompositeData(_columnReqRowType, success, failures, errors, total));
    }
  }

  public TabularDataSupport getResultsBySecurityType() {
    return _resultsBySecurityType;
  }

  public TabularDataSupport getResultsByColumnRequirement() {
    return _resultsByColumnRequirement;
  }
  
  public int getSuccesses() {
    return _successes.intValue();
  }
  
  public int getFailures() {
    return _failures.intValue();
  }
  
  
  public int getTotal() {
    return _total.intValue();
  }
  
}
