/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.io.CharArrayWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Describes a resolution failure. The implementation is intended to provide low-cost construction of failure information, at the cost of a more complex querying/inspection algorithm.
 */
public final class ResolutionFailureImpl extends ResolutionFailure {

  private final ValueRequirement _valueRequirement;
  private final List<Object> _events = new LinkedList<Object>();

  // Construction

  private ResolutionFailureImpl(final ValueRequirement valueRequirement) {
    _valueRequirement = valueRequirement;
  }

  protected static ResolutionFailure recursiveRequirement(final ValueRequirement valueRequirement) {
    return new ResolutionFailureImpl(valueRequirement).appendEvent(Status.RECURSIVE_REQUIREMENT);
  }

  protected static ResolutionFailure functionApplication(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification outputSpecification) {
    return functionApplication(valueRequirement, function.getFunction().getFunctionDefinition().getUniqueId(), outputSpecification);
  }

  protected static ResolutionFailure functionApplication(final ValueRequirement valueRequirement, final String function, final ValueSpecification outputSpecification) {
    return new ResolutionFailureImpl(valueRequirement).appendEvent(function).appendEvent(outputSpecification);
  }

  protected static ResolutionFailure noFunctions(final ValueRequirement valueRequirement) {
    return new ResolutionFailureImpl(valueRequirement).appendEvent(Status.NO_FUNCTIONS);
  }

  protected static ResolutionFailure couldNotResolve(final ValueRequirement valueRequirement) {
    return new ResolutionFailureImpl(valueRequirement).appendEvent(Status.COULD_NOT_RESOLVE);
  }

  protected static ResolutionFailure unsatisfied(final ValueRequirement valueRequirement) {
    return new ResolutionFailureImpl(valueRequirement).appendEvent(Status.UNSATISFIED);
  }

  protected static ResolutionFailure marketDataMissing(final ValueRequirement valueRequirement) {
    return new ResolutionFailureImpl(valueRequirement).appendEvent(Status.MARKET_DATA_MISSING);
  }

  @Override
  protected ResolutionFailure additionalRequirement(final ValueRequirement valueRequirement, final ResolutionFailure failure) {
    return appendEvent(Status.ADDITIONAL_REQUIREMENT).requirement(valueRequirement, failure);
  }

  @Override
  protected ResolutionFailure requirement(final ValueRequirement valueRequirement, final ResolutionFailure failure) {
    return appendEvent((failure != null) ? failure : valueRequirement);
  }

  @Override
  protected ResolutionFailure requirements(final Map<ValueSpecification, ValueRequirement> available) {
    return appendEvent(available);
  }

  @Override
  protected ResolutionFailure getResultsFailed() {
    return appendEvent(Status.GET_RESULTS_FAILED);
  }

  @Override
  protected ResolutionFailure getAdditionalRequirementsFailed() {
    return appendEvent(Status.GET_ADDITIONAL_REQUIREMENTS_FAILED);
  }

  @Override
  protected ResolutionFailure lateResolutionFailure() {
    return appendEvent(Status.LATE_RESOLUTION_FAILURE);
  }

  @Override
  protected ResolutionFailure getRequirementsFailed() {
    return appendEvent(Status.GET_REQUIREMENTS_FAILED);
  }

  @Override
  protected ResolutionFailure suppressed() {
    return appendEvent(Status.SUPPRESSED);
  }

  @Override
  protected ResolutionFailure checkFailure(final ValueRequirement valueRequirement) {
    return this;
  }

  private synchronized ResolutionFailureImpl appendEvent(final Object event) {
    _events.add(event);
    return this;
  }

  // Query

  @Override
  public ValueRequirement getValueRequirement() {
    return _valueRequirement;
  }

  public List<Object> getEvents() {
    return _events;
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized <T> Collection<T> accept(final ResolutionFailureVisitor<T> visitor) {
    final LinkedList<T> result = new LinkedList<T>();
    final Iterator<?> itr = _events.iterator();
    String function = null;
    ValueSpecification outputSpecification = null;
    final Map<ValueSpecification, ValueRequirement> satisfied = new HashMap<ValueSpecification, ValueRequirement>();
    final Set<ResolutionFailure> unsatisfied = new HashSet<ResolutionFailure>();
    final Set<ResolutionFailure> unsatisfiedAdditional = new HashSet<ResolutionFailure>();
    while (itr.hasNext()) {
      final Object event = itr.next();
      if (event instanceof String) {
        if (function != null) {
          result.add(visitor.visitFunction(getValueRequirement(), function, outputSpecification, satisfied, unsatisfied, unsatisfiedAdditional));
        }
        function = (String) event;
        outputSpecification = (ValueSpecification) itr.next();
        satisfied.clear();
        unsatisfied.clear();
        unsatisfiedAdditional.clear();
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
              result.add(visitor.visitFunction(getValueRequirement(), function, outputSpecification, satisfied, unsatisfied, unsatisfiedAdditional));
              function = null;
            }
            result.add(visitor.visitCouldNotResolve(getValueRequirement()));
            break;
          case GET_ADDITIONAL_REQUIREMENTS_FAILED:
            assert function != null;
            result.add(visitor.visitGetAdditionalRequirementsFailed(getValueRequirement(), function, outputSpecification, satisfied));
            function = null;
            break;
          case GET_RESULTS_FAILED:
            assert function != null;
            result.add(visitor.visitGetResultsFailed(getValueRequirement(), function, outputSpecification, satisfied));
            function = null;
            break;
          case GET_REQUIREMENTS_FAILED:
            assert function != null;
            result.add(visitor.visitGetRequirementsFailed(getValueRequirement(), function, outputSpecification));
            function = null;
            break;
          case LATE_RESOLUTION_FAILURE:
            assert function != null;
            result.add(visitor.visitLateResolutionFailure(getValueRequirement(), function, outputSpecification, satisfied));
            function = null;
            break;
          case MARKET_DATA_MISSING:
            if (function != null) {
              result.add(visitor.visitFunction(getValueRequirement(), function, outputSpecification, satisfied, unsatisfied, unsatisfiedAdditional));
              function = null;
            }
            result.add(visitor.visitMarketDataMissing(getValueRequirement()));
            break;
          case NO_FUNCTIONS:
            if (function != null) {
              result.add(visitor.visitFunction(getValueRequirement(), function, outputSpecification, satisfied, unsatisfied, unsatisfiedAdditional));
              function = null;
            }
            result.add(visitor.visitNoFunctions(getValueRequirement()));
            break;
          case RECURSIVE_REQUIREMENT:
            if (function != null) {
              result.add(visitor.visitFunction(getValueRequirement(), function, outputSpecification, satisfied, unsatisfied, unsatisfiedAdditional));
              function = null;
            }
            result.add(visitor.visitRecursiveRequirement(getValueRequirement()));
            break;
          case SUPPRESSED:
            assert function != null;
            result.add(visitor.visitBlacklistSuppressed(getValueRequirement(), function, outputSpecification, satisfied));
            function = null;
            break;
          case UNSATISFIED:
            if (function != null) {
              result.add(visitor.visitFunction(getValueRequirement(), function, outputSpecification, satisfied, unsatisfied, unsatisfiedAdditional));
              function = null;
            }
            result.add(visitor.visitUnsatisfied(getValueRequirement()));
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
      result.add(visitor.visitFunction(getValueRequirement(), function, outputSpecification, satisfied, unsatisfied, unsatisfiedAdditional));
    }
    return result;
  }

  // Composition

  /**
   * Merge the causes of failure from the other into this.
   * 
   * @param failureRef cause of failure
   */
  @Override
  protected synchronized void merge(final ResolutionFailure failureRef) {
    final ResolutionFailureImpl failure = (ResolutionFailureImpl) failureRef;
    synchronized (failure) {
      final Iterator<Object> itrNew = failure._events.iterator();
      Object eventNew = itrNew.next();
      do {
        if (eventNew instanceof String) {
          final String function = (String) eventNew;
          final ValueSpecification outputSpecification = (ValueSpecification) itrNew.next();
          // Extract the events that correspond to this function application
          final List<Object> newEvents = new LinkedList<Object>();
          //CSOFF
          scan:
          // CSON
          do {
            eventNew = itrNew.next();
            if (eventNew instanceof String) {
              break scan;
            } else if (eventNew instanceof Status) {
              switch ((Status) eventNew) {
                case COULD_NOT_RESOLVE:
                case MARKET_DATA_MISSING:
                case NO_FUNCTIONS:
                case RECURSIVE_REQUIREMENT:
                case UNSATISFIED:
                  break scan;
              }
            }
            newEvents.add(eventNew);
            if (!itrNew.hasNext()) {
              eventNew = null;
              break scan;
            }
          } while (true);
          // If the function application already exists, append the events
          final ListIterator<Object> itrThis = _events.listIterator();
          boolean matched = false;
          //CSOFF
          scanStartEvent:
          //CSON
          while (itrThis.hasNext()) {
            Object eventThis = itrThis.next();
            if (function.equals(eventThis)) {
              eventThis = itrThis.next();
              if (outputSpecification.equals(eventThis)) {
                // Have found a match; consider the existing failure events
                //CSOFF
                scanFailureEvent:
                //CSON
                while (itrThis.hasNext()) {
                  eventThis = itrThis.next();
                  if (eventThis instanceof String) {
                    itrThis.previous();
                    break scanFailureEvent;
                  } else if (eventThis instanceof Status) {
                    switch ((Status) eventThis) {
                      case ADDITIONAL_REQUIREMENT:
                        // Discard any matching "new" event
                        eventThis = itrThis.next();
                        final ListIterator<Object> itrNewEvents = newEvents.listIterator();
                        while (itrNewEvents.hasNext()) {
                          Object newEvent = itrNewEvents.next();
                          if (newEvent == Status.ADDITIONAL_REQUIREMENT) {
                            newEvent = itrNewEvents.next();
                            if (eventThis.equals(newEvent)) {
                              itrNewEvents.remove();
                              itrNewEvents.previous();
                              itrNewEvents.remove();
                              break;
                            }
                          }
                        }
                        break;
                      case GET_ADDITIONAL_REQUIREMENTS_FAILED:
                      case GET_RESULTS_FAILED:
                      case GET_REQUIREMENTS_FAILED:
                      case LATE_RESOLUTION_FAILURE:
                      case SUPPRESSED:
                        // Discard any matching "new" event
                        newEvents.remove(eventThis);
                        continue scanStartEvent;
                      case COULD_NOT_RESOLVE:
                      case MARKET_DATA_MISSING:
                      case NO_FUNCTIONS:
                      case RECURSIVE_REQUIREMENT:
                      case UNSATISFIED:
                        itrThis.previous();
                        break scanFailureEvent;
                      default:
                        throw new IllegalStateException("event = " + eventThis);
                    }
                  } else {
                    // Discard any matching "new" event
                    final Iterator<Object> itrNewEvents = newEvents.iterator();
                    while (itrNewEvents.hasNext()) {
                      final Object newEvent = itrNewEvents.next();
                      if (eventThis.equals(newEvent)) {
                        itrNewEvents.remove();
                        break;
                      } else if (newEvent == Status.ADDITIONAL_REQUIREMENT) {
                        itrNewEvents.next();
                      }
                    }
                  }
                }
                // Iterator is now positioned just before the next "start" event
                for (Object newEvent : newEvents) {
                  itrThis.add(newEvent);
                }
                matched = true;
                break;
              }
            }
          }
          // If the function application didn't exist, append the application and events
          if (!matched && !newEvents.isEmpty()) {
            _events.add(function);
            _events.add(outputSpecification);
            _events.addAll(newEvents);
          }
        } else if (eventNew instanceof Status) {
          if (!_events.contains(eventNew)) {
            _events.add(eventNew);
          }
          if (itrNew.hasNext()) {
            eventNew = itrNew.next();
          } else {
            eventNew = null;
          }
        } else {
          throw new IllegalStateException("event = " + eventNew);
        }
      } while (eventNew != null);
    }
  }

  // Misc

  @Override
  public String toString() {
    final CharArrayWriter writer = new CharArrayWriter();
    accept(new ResolutionFailurePrinter(writer));
    return writer.toString();
  }

  @Override
  public synchronized Object clone() {
    final ResolutionFailureImpl copy = new ResolutionFailureImpl(getValueRequirement());
    copy._events.addAll(_events);
    return copy;
  }

  /**
   * Tests this resolution failure object with another for equality. Note that the caller must ensure that the monitor for both is held, or a suitable exclusion lock is held at an outer level.
   * 
   * @param obj object to compare to
   * @return true if the objects are equal
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof ResolutionFailureImpl)) {
      return false;
    }
    final ResolutionFailureImpl other = (ResolutionFailureImpl) obj;
    return getValueRequirement().equals(other.getValueRequirement())
        && _events.equals(other._events);
  }

  @Override
  public int hashCode() {
    return getValueRequirement().hashCode();
  }

}
