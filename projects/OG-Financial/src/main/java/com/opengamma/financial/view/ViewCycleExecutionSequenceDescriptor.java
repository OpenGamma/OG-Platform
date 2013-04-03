/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;

/**
 * Abstraction of a {@link ViewCycleExecutionSequence} that can be used as part of an evaluation target. Construction of the actual execution sequence is deferred until graph execution time as it may
 * depend on the valuation time or other runtime parameters that can't be captured at graph build time or shared between view processes.
 * <p>
 * Implementations must include an {@code equals} method such that two sequence descriptors are equal if they will produce equal sequences for the same execution context
 */
public interface ViewCycleExecutionSequenceDescriptor {

  ViewCycleExecutionSequence createSequence(FunctionExecutionContext executionContext);

}
