/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Describes a resolution failure. The implementation is intended to provide low-cost construction of failure information, at the
 * cost of a more complex querying/inspection algorithm.  
 */
public final class ResolutionFailure implements Cloneable {

  private static final Logger s_logger = LoggerFactory.getLogger(ResolutionFailure.class);

  private static enum Status {
    ADDITIONAL_REQUIREMENT,
    COULD_NOT_RESOLVE,
    GET_ADDITIONAL_REQUIREMENTS_FAILED,
    GET_RESULTS_FAILED,
    GET_REQUIREMENTS_FAILED,
    LATE_RESOLUTION_FAILURE,
    NO_FUNCTIONS,
    RECURSIVE_REQUIREMENT,
    UNSATISFIED
  }

  private final ValueRequirement _valueRequirement;
  private final LinkedList<Object> _events = new LinkedList<Object>();
  private int _resultCount;

  // Construction

  private ResolutionFailure(final ValueRequirement valueRequirement) {
    _valueRequirement = valueRequirement;
  }

  protected static ResolutionFailure recursiveRequirement(final ValueRequirement valueRequirement) {
    return new ResolutionFailure(valueRequirement).appendEvent(Status.RECURSIVE_REQUIREMENT);
  }

  protected static ResolutionFailure functionApplication(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification outputSpecification) {
    return new ResolutionFailure(valueRequirement).appendEvent(function).appendEvent(outputSpecification);
  }

  protected static ResolutionFailure resolvedValue(final ValueRequirement valueRequirement, final ResolvedValue value) {
    return new ResolutionFailure(valueRequirement).appendEvent(value);
  }

  protected static ResolutionFailure noFunctions(final ValueRequirement valueRequirement) {
    return new ResolutionFailure(valueRequirement).appendEvent(Status.NO_FUNCTIONS);
  }

  protected static ResolutionFailure couldNotResolve(final ValueRequirement valueRequirement) {
    return new ResolutionFailure(valueRequirement).appendEvent(Status.COULD_NOT_RESOLVE);
  }

  protected static ResolutionFailure unsatisfied(final ValueRequirement valueRequirement) {
    return new ResolutionFailure(valueRequirement).appendEvent(Status.UNSATISFIED);
  }

  protected ResolutionFailure additionalRequirement(final ValueRequirement valueRequirement, final ResolutionFailure failure) {
    return appendEvent(Status.ADDITIONAL_REQUIREMENT).requirement(valueRequirement, failure);
  }

  protected ResolutionFailure requirement(final ValueRequirement valueRequirement, final ResolutionFailure failure) {
    return appendEvent((failure != null) ? failure : valueRequirement);
  }

  protected ResolutionFailure requirements(final Map<ValueSpecification, ValueRequirement> available) {
    return appendEvent(available);
  }

  protected ResolutionFailure getResultsFailed() {
    return appendEvent(Status.GET_RESULTS_FAILED);
  }

  protected ResolutionFailure getAdditionalRequirementsFailed() {
    return appendEvent(Status.GET_ADDITIONAL_REQUIREMENTS_FAILED);
  }

  protected ResolutionFailure lateResolutionFailure() {
    return appendEvent(Status.LATE_RESOLUTION_FAILURE);
  }

  protected ResolutionFailure getRequirementsFailed() {
    return appendEvent(Status.GET_REQUIREMENTS_FAILED);
  }

  private synchronized ResolutionFailure appendEvent(final Object event) {
    _events.add(event);
    return this;
  }

  // Query

  public ValueRequirement getValueRequirement() {
    return _valueRequirement;
  }

  @SuppressWarnings("unchecked")
  public synchronized void accept(final ResolutionFailureVisitor visitor) {
    final Iterator<?> itr = _events.iterator();
    ParameterizedFunction function = null;
    ValueSpecification outputSpecification = null;
    final Map<ValueSpecification, ValueRequirement> satisfied = new HashMap<ValueSpecification, ValueRequirement>();
    final Set<ResolutionFailure> unsatisfied = new HashSet<ResolutionFailure>();
    final Set<ResolutionFailure> unsatisfiedAdditional = new HashSet<ResolutionFailure>();
    while (itr.hasNext()) {
      final Object event = itr.next();
      if (event instanceof ParameterizedFunction) {
        if (function != null) {
          visitor.visitFunction(getValueRequirement(), function, outputSpecification, satisfied, unsatisfied, unsatisfiedAdditional);
        }
        function = (ParameterizedFunction) event;
        outputSpecification = (ValueSpecification) itr.next();
        satisfied.clear();
        unsatisfied.clear();
        unsatisfiedAdditional.clear();
      } else if (event instanceof ResolvedValue) {
        if (function != null) {
          visitor.visitFunction(getValueRequirement(), function, outputSpecification, satisfied, unsatisfied, unsatisfiedAdditional);
          function = null;
        }
        visitor.visitResolvedValue(getValueRequirement(), (ResolvedValue) event);
      } else if (event instanceof Status) {
        switch ((Status) event) {
          case ADDITIONAL_REQUIREMENT: {
            assert function != null;
            final Object req = itr.next();
            if (req instanceof ResolutionFailure) {
              unsatisfiedAdditional.add((ResolutionFailure) req);
            } else {
              unsatisfiedAdditional.add(unsatisfied((ValueRequirement) req));
            }
            break;
          }
          case COULD_NOT_RESOLVE:
            if (function != null) {
              visitor.visitFunction(getValueRequirement(), function, outputSpecification, satisfied, unsatisfied, unsatisfiedAdditional);
              function = null;
            }
            visitor.visitCouldNotResolve(getValueRequirement());
            break;
          case GET_ADDITIONAL_REQUIREMENTS_FAILED:
            assert function != null;
            visitor.visitGetAdditionalRequirementsFailed(getValueRequirement(), function, outputSpecification, satisfied);
            function = null;
            break;
          case GET_RESULTS_FAILED:
            assert function != null;
            visitor.visitGetResultsFailed(getValueRequirement(), function, outputSpecification);
            function = null;
            break;
          case GET_REQUIREMENTS_FAILED:
            assert function != null;
            visitor.visitGetRequirementsFailed(getValueRequirement(), function, outputSpecification);
            function = null;
            break;
          case LATE_RESOLUTION_FAILURE:
            assert function != null;
            visitor.visitLateResolutionFailure(getValueRequirement(), function, outputSpecification, satisfied);
            function = null;
            break;
          case NO_FUNCTIONS:
            if (function != null) {
              visitor.visitFunction(getValueRequirement(), function, outputSpecification, satisfied, unsatisfied, unsatisfiedAdditional);
              function = null;
            }
            visitor.visitNoFunctions(getValueRequirement());
            break;
          case RECURSIVE_REQUIREMENT:
            if (function != null) {
              visitor.visitFunction(getValueRequirement(), function, outputSpecification, satisfied, unsatisfied, unsatisfiedAdditional);
              function = null;
            }
            visitor.visitRecursiveRequirement(getValueRequirement());
            break;
          case UNSATISFIED:
            if (function != null) {
              visitor.visitFunction(getValueRequirement(), function, outputSpecification, satisfied, unsatisfied, unsatisfiedAdditional);
              function = null;
            }
            visitor.visitUnsatisfied(getValueRequirement());
            break;
          default:
            throw new IllegalStateException("event = " + event);
        }
      } else if (event instanceof ValueRequirement) {
        assert function != null;
        unsatisfied.add(unsatisfied((ValueRequirement) event));
      } else if (event instanceof ResolutionFailure) {
        assert function != null;
        unsatisfied.add((ResolutionFailure) event);
      } else if (event instanceof Map<?, ?>) {
        assert function != null;
        satisfied.putAll((Map<ValueSpecification, ValueRequirement>) event);
      } else {
        throw new IllegalStateException("event = " + event);
      }
    }
    if (function != null) {
      visitor.visitFunction(getValueRequirement(), function, outputSpecification, satisfied, unsatisfied, unsatisfiedAdditional);
    }
  }

  // Composition

  protected synchronized int getResultCount() {
    return _resultCount;
  }

  protected synchronized void resolvedValue(final ResolvedValue value) {
    appendEvent(value);
    _resultCount++;
  }

  /**
   * Merge the causes of failure from the other into this.
   * 
   * @param failure cause of failure
   */
  protected synchronized void merge(final ResolutionFailure failure) {
    assert getValueRequirement().getTargetSpecification().equals(failure.getValueRequirement().getTargetSpecification())
        && getValueRequirement().getValueName().equals(failure.getValueRequirement().getValueName());
    synchronized (failure) {
      final Iterator<Object> itrThis = _events.descendingIterator();
      final Iterator<Object> itrNew = failure._events.descendingIterator();
      boolean match = true;
      while (itrNew.hasNext() && itrThis.hasNext()) {
        if (!itrThis.next().equals(itrNew.next())) {
          match = false;
          break;
        }
      }
      if (match) {
        s_logger.debug("Discarding merge of {} event(s) into existing {} event(s)", failure._events.size(), _events.size());
      } else {
        _events.addAll(failure._events);
      }
    }
  }

  // Misc

  @Override
  public String toString() {
    return "ResolutionFailure[" + _valueRequirement + "]";
  }

  @Override
  public synchronized Object clone() {
    final ResolutionFailure copy = new ResolutionFailure(getValueRequirement());
    copy._resultCount = _resultCount;
    copy._events.addAll(_events);
    return copy;
  }

  /**
   * Tests this resolution failure object with another for equality. Note that the caller must ensure that the monitor for
   * both is held, or a suitable exclusion lock is held at an outer level.
   * 
   * @param obj  object to compare to
   * @return true if the objects are equal
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof ResolutionFailure)) {
      return false;
    }
    final ResolutionFailure other = (ResolutionFailure) obj;
    return getValueRequirement().equals(other.getValueRequirement())
        && _events.equals(other._events);
  }

  @Override
  public int hashCode() {
    return getValueRequirement().hashCode();
  }

}
