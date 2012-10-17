/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Visitor for printing out resolution failure information to a file.
 */
public class ResolutionFailurePrinter extends ResolutionFailureVisitor<Void> {

  private final Set<ResolutionFailure> _visited;
  private final PrintWriter _out;
  private final String _indent;

  public ResolutionFailurePrinter() {
    this(new OutputStreamWriter(System.out));
  }

  public ResolutionFailurePrinter(final Writer out) {
    ArgumentChecker.notNull(out, "out");
    _out = new PrintWriter(out, true);
    _indent = "";
    _visited = Collections.newSetFromMap(new IdentityHashMap<ResolutionFailure, Boolean>());
  }

  protected ResolutionFailurePrinter(final ResolutionFailurePrinter parent, final PrintWriter writer) {
    ArgumentChecker.notNull(parent, "parent");
    _out = writer;
    _indent = parent._indent + "  ";
    _visited = parent._visited;
  }

  protected void println(final String str) {
    synchronized (_out) {
      _out.print(_indent);
      _out.println(str);
    }
  }

  protected String toString(final ValueRequirement valueRequirement) {
    final StringBuilder sb = new StringBuilder();
    sb.append(valueRequirement.getValueName());
    if (!valueRequirement.getConstraints().isEmpty()) {
      sb.append(' ').append(toString(valueRequirement.getConstraints()));
    }
    sb.append(" on ").append(toString(valueRequirement.getTargetReference()));
    return sb.toString();
  }

  protected String toString(final ComputationTargetReference target) {
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
  protected Void visitCouldNotResolve(final ValueRequirement valueRequirement) {
    println("Could not resolve " + toString(valueRequirement));
    return null;
  }

  @Override
  protected Void visitNoFunctions(final ValueRequirement valueRequirement) {
    println("No functions available for " + toString(valueRequirement));
    return null;
  }

  @Override
  protected Void visitRecursiveRequirement(final ValueRequirement valueRequirement) {
    println("Recursive requirement on " + toString(valueRequirement) + " for function(s) producing it");
    return null;
  }

  @Override
  protected Void visitUnsatisfied(final ValueRequirement valueRequirement) {
    println("Unsatisfied requirement " + toString(valueRequirement));
    return null;
  }

  @Override
  protected Void visitMarketDataMissing(final ValueRequirement valueRequirement) {
    println("Market data missing for requirement " + toString(valueRequirement));
    return null;
  }

  @Override
  protected Void visitSuccessfulFunction(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied) {
    println("Applied " + function + " to produce " + toString(desiredOutput) + " for " + toString(valueRequirement));
    return null;
  }

  @Override
  protected Void visitFailedFunction(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied) {
    final StringWriter buffer = new StringWriter();
    final ResolutionFailurePrinter visitor = new ResolutionFailurePrinter(this, new PrintWriter(buffer));
    for (ResolutionFailure requirement : unsatisfied) {
      if (_visited.add(requirement)) {
        requirement.accept(visitor);
      } else {
        visitor.println("... " + requirement.getValueRequirement());
      }
    }
    synchronized (_out) {
      _out.print(_indent);
      _out.println("Couldn't satisfy " + toStringResolutionFailures(unsatisfied) + " for " + function + " to produce " + toString(desiredOutput) + " for " + toString(valueRequirement) +
          ". Caused by:");
      _out.print(buffer.toString());
    }
    return null;
  }

  @Override
  protected Void visitGetAdditionalRequirementsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    println("getAdditionalRequirements method failed on " + function + " with inputs " + toString(requirements) + " to produce " + toString(desiredOutput) + " for " +
        toString(valueRequirement));
    return null;
  }

  @Override
  protected Void visitGetResultsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    println("getResults method failed on " + function + " with inputs " + toString(requirements) + " to produce " + toString(desiredOutput) + " for " + toString(valueRequirement));
    return null;
  }

  @Override
  protected Void visitGetRequirementsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput) {
    println("getRequirements method failed on " + function + " to produce " + toString(desiredOutput) + " for " + toString(valueRequirement));
    return null;
  }

  @Override
  protected Void visitLateResolutionFailure(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    println("Provisional result " + toString(desiredOutput) + " for " + toString(desiredOutput) + " not in output of " + function + " after late resolution of " +
        toStringValueSpecifications(requirements.keySet()));
    return null;
  }

}
