/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Visitor for printing out resolution failure information to a file.
 */
public class ResolutionFailurePrinter extends ResolutionFailureVisitor<Void> {

  private final PrintStream _out;
  private final String _indent;

  public ResolutionFailurePrinter() {
    this(System.out);
  }

  public ResolutionFailurePrinter(final PrintStream out) {
    this("", out);
  }

  protected ResolutionFailurePrinter(final String indent, final PrintStream out) {
    ArgumentChecker.notNull(indent, "indent");
    ArgumentChecker.notNull(out, "out");
    _out = out;
    _indent = indent;
  }

  protected ResolutionFailurePrinter indent() {
    return new ResolutionFailurePrinter(_indent + "  ", _out);
  }

  protected void println(final String str) {
    _out.print(_indent);
    _out.println(str);
  }

  protected String toString(final ValueRequirement valueRequirement) {
    final StringBuilder sb = new StringBuilder();
    sb.append(valueRequirement.getValueName());
    if (!valueRequirement.getConstraints().isEmpty()) {
      sb.append(' ').append(toString(valueRequirement.getConstraints()));
    }
    sb.append(" on ").append(toString(valueRequirement.getTargetSpecification()));
    return sb.toString();
  }

  protected String toString(final ComputationTargetSpecification target) {
    return target.toString();
  }

  protected String toString(final ResolvedValue resolvedValue) {
    return toString(resolvedValue.getValueSpecification());
  }

  protected String toString(final ValueProperties valueProperties) {
    return valueProperties.toString();
  }

  protected String toString(final ValueSpecification valueSpecification) {
    return valueSpecification.getValueName() + ' ' + toString(valueSpecification.getProperties()) + " on " + toString(valueSpecification.getTargetSpecification());
  }

  protected String toString(final ParameterizedFunction function) {
    return function.toString();
  }

  protected String toString(final Map<ValueSpecification, ValueRequirement> requirements) {
    final StringBuilder sb = new StringBuilder();
    boolean comma = false;
    for (ValueSpecification requirement : requirements.keySet()) {
      if (comma) {
        sb.append(", ");
      } else {
        sb.append("{");
        comma = true;
      }
      sb.append(toString(requirement));
    }
    if (comma) {
      sb.append("}");
    } else {
      sb.append("EMPTY");
    }
    return sb.toString();
  }

  protected String toStringResolutionFailures(final Set<ResolutionFailure> failures) {
    final StringBuilder sb = new StringBuilder();
    boolean comma = false;
    for (ResolutionFailure failure : failures) {
      if (comma) {
        sb.append(", ");
      } else {
        sb.append("{");
        comma = true;
      }
      sb.append(toString(failure));
    }
    if (comma) {
      sb.append("}");
    } else {
      sb.append("EMPTY");
    }
    return sb.toString();
  }

  protected String toStringValueSpecifications(final Set<ValueSpecification> specifications) {
    final StringBuilder sb = new StringBuilder();
    boolean comma = false;
    for (ValueSpecification specification : specifications) {
      if (comma) {
        sb.append(", ");
      } else {
        sb.append("{");
        comma = true;
      }
      sb.append(toString(specification));
    }
    if (comma) {
      sb.append("}");
    } else {
      sb.append("EMPTY");
    }
    return sb.toString();
  }

  protected String toString(final ResolutionFailure failure) {
    return toString(failure.getValueRequirement());
  }

  @Override
  protected synchronized Void visitCouldNotResolve(final ValueRequirement valueRequirement) {
    println("Could not resolve " + toString(valueRequirement));
    return null;
  }

  @Override
  protected synchronized Void visitNoFunctions(final ValueRequirement valueRequirement) {
    println("No functions available for " + toString(valueRequirement));
    return null;
  }

  @Override
  protected synchronized Void visitRecursiveRequirement(final ValueRequirement valueRequirement) {
    println("Recursive requirement on " + toString(valueRequirement) + " for function(s) producing it");
    return null;
  }

  @Override
  protected synchronized Void visitUnsatisfied(final ValueRequirement valueRequirement) {
    println("Unsatisfied requirement " + toString(valueRequirement));
    return null;
  }

  @Override
  protected synchronized Void visitMarketDataMissing(final ValueRequirement valueRequirement) {
    println("Market data missing for requirement " + toString(valueRequirement));
    return null;
  }

  @Override
  protected synchronized Void visitSuccessfulFunction(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied) {
    println("Applied " + toString(function) + " to produce " + toString(desiredOutput) + " for " + toString(valueRequirement));
    return null;
  }

  @Override
  protected synchronized Void visitFailedFunction(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied) {
    println("Couldn't satisfy " + toStringResolutionFailures(unsatisfied) + " to produce " + toString(desiredOutput) + " for " + toString(valueRequirement) + ". Caused by:");
    for (ResolutionFailure requirement : unsatisfied) {
      requirement.accept(indent());
    }
    return null;
  }

  @Override
  protected synchronized Void visitGetAdditionalRequirementsFailed(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    println("getAdditionalRequirements method failed on " + toString(function) + " with inputs " + toString(requirements) + " to produce " + toString(desiredOutput) + " for " +
        toString(valueRequirement));
    return null;
  }

  @Override
  protected synchronized Void visitGetResultsFailed(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput) {
    println("getResults method failed on " + toString(function) + " to produce " + toString(desiredOutput) + " for " + toString(valueRequirement));
    return null;
  }

  @Override
  protected synchronized Void visitGetRequirementsFailed(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput) {
    println("getRequirements method failed on " + toString(function) + " to produce " + toString(desiredOutput) + " for " + toString(valueRequirement));
    return null;
  }

  @Override
  protected synchronized Void visitLateResolutionFailure(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    println("Provisional result " + toString(desiredOutput) + " for " + toString(desiredOutput) + " not in output of " + toString(function) + " after late resolution of " +
        toStringValueSpecifications(requirements.keySet()));
    return null;
  }

}
