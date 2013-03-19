/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link DefaultFunctionBlacklistMaintainer} class.
 */
@Test(groups = TestGroup.UNIT)
public class DefaultFunctionBlacklistMaintainerTest {

  private final String _functionIdentifier1 = "F1";
  private final String _functionIdentifier2 = "F2";
  private final ComputationTarget _target = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "Test"));
  private final FunctionParameters _functionParameters = new EmptyFunctionParameters();
  private final ComputationTargetSpecification _targetSpecification = _target.toSpecification();
  private final Set<ValueSpecification> _inputs = Collections.<ValueSpecification>emptySet();
  private final Set<ValueSpecification> _outputs = Collections.<ValueSpecification>emptySet();
  private final CalculationJobItem _jobItem1 = new CalculationJobItem(_functionIdentifier1, _functionParameters, _targetSpecification, _inputs, _outputs, ExecutionLogMode.INDICATORS);
  private final CalculationJobItem _jobItem2 = new CalculationJobItem(_functionIdentifier2, _functionParameters, _targetSpecification, _inputs, _outputs, ExecutionLogMode.INDICATORS);

  public void testEmptyPolicy() {
    final FunctionBlacklistPolicy policy = new EmptyFunctionBlacklistPolicy();
    final ManageableFunctionBlacklist update = Mockito.mock(ManageableFunctionBlacklist.class);
    final FunctionBlacklistMaintainer maintainer = new DefaultFunctionBlacklistMaintainer(policy, update);
    maintainer.failedJobItem(_jobItem1);
    maintainer.failedJobItem(_jobItem2);
    // Empty policy means no rules added
    Mockito.verifyZeroInteractions(update);
  }

  public void testSimplePolicy() {
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      final Collection<FunctionBlacklistPolicy.Entry> entries = new ArrayList<FunctionBlacklistPolicy.Entry>();
      entries.add(FunctionBlacklistPolicy.Entry.WILDCARD.activationPeriod(5));
      entries.add(FunctionBlacklistPolicy.Entry.FUNCTION.activationPeriod(10));
      entries.add(FunctionBlacklistPolicy.Entry.PARAMETERIZED_FUNCTION.activationPeriod(30));
      entries.add(FunctionBlacklistPolicy.Entry.PARTIAL_NODE.activationPeriod(60));
      entries.add(FunctionBlacklistPolicy.Entry.EXECUTION_NODE);
      final FunctionBlacklistPolicy policy = new DefaultFunctionBlacklistPolicy(UniqueId.of("Test", "Test"), 120, entries);
      final Map<FunctionBlacklistRule, Integer> rules = new HashMap<FunctionBlacklistRule, Integer>();
      final ManageableFunctionBlacklist update = new AbstractManageableFunctionBlacklist("Test", executor, 0) {

        @Override
        public Set<FunctionBlacklistRule> getRules() {
          throw new UnsupportedOperationException();
        }

        @Override
        public void removeBlacklistRule(final FunctionBlacklistRule rule) {
          throw new UnsupportedOperationException();
        }

        @Override
        public void addBlacklistRule(final FunctionBlacklistRule rule, final int timeToLive) {
          rules.put(rule, timeToLive);
        }

      };
      final FunctionBlacklistMaintainer maintainer = new DefaultFunctionBlacklistMaintainer(policy, update);
      maintainer.failedJobItem(_jobItem1);
      maintainer.failedJobItem(_jobItem2);
      assertEquals(rules.size(), 9);
      final FunctionBlacklistRule rule = new FunctionBlacklistRule();
      assertEquals((long) rules.get(rule), 5); // WILDCARD
      rule.setFunctionIdentifier(_functionIdentifier1);
      assertEquals((long) rules.get(rule), 10); // FUNCTION-1
      rule.setFunctionParameters(_functionParameters);
      assertEquals((long) rules.get(rule), 30); // PARAMETERIZED_FUNCTION-1
      rule.setTarget(_targetSpecification);
      assertEquals((long) rules.get(rule), 60); // PARTIAL_NODE-1
      rule.setInputs(_inputs);
      rule.setOutputs(_outputs);
      assertEquals((long) rules.get(rule), 120); // EXACT_NODE-2
      rule.setFunctionIdentifier(_functionIdentifier2);
      assertEquals((long) rules.get(rule), 120); // EXACT_NODE-2
      rule.setInputs(null);
      rule.setOutputs(null);
      assertEquals((long) rules.get(rule), 60); // PARTIAL_NODE-2
      rule.setTarget(null);
      assertEquals((long) rules.get(rule), 30); // PARAMETERIZED_FUNCTION-2
      rule.setFunctionParameters(null);
      assertEquals((long) rules.get(rule), 10); // FUNCTION-2
    } finally {
      executor.shutdown();
    }
  }

}
