/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.convert.ValueConversionContext;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Converts a value using some basic rules.
 */
public class DefaultValueConverter extends ValueConverter {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultValueConverter.class);

  private static final class State {

    private final JavaTypeInfo<?> _targetType;
    private final TypeConverter _nextStateConverter;
    private final State _nextState;
    private final int _cost;

    public State(final JavaTypeInfo<?> targetType, final TypeConverter nextStateConverter, final State nextState, final int thisCost) {
      _targetType = targetType;
      _nextStateConverter = nextStateConverter;
      _nextState = nextState;
      _cost = (nextState != null) ? nextState.getCost() + thisCost : thisCost;
    }

    public JavaTypeInfo<?> getTargetType() {
      return _targetType;
    }

    public TypeConverter getNextStateConverter() {
      return _nextStateConverter;
    }

    public State getNextState() {
      return _nextState;
    }

    public int getCost() {
      return _cost;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append("TargetType = ").append(_targetType).append(", NextStateConverter = ").append(_nextStateConverter).append(", Cost = ").append(_cost);
      return sb.toString();
    }

    // TODO: build a set to speed up the visit lookups

    public boolean visited(final TypeConverter converter) {
      State s = this;
      do {
        if (converter == s._nextStateConverter) {
          return true;
        }
        s = s._nextState;
      } while (s != null);
      return false;
    }

    public boolean visited(final JavaTypeInfo<?> type) {
      State s = this;
      do {
        if (type == s._targetType) {
          return true;
        }
        s = s._nextState;
      } while (s != null);
      return false;
    }

    public JavaTypeInfo<?> getRootType() {
      State s = this;
      while (s._nextState != null) {
        s = s._nextState;
      }
      return s._targetType;
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof State)) {
        return false;
      }
      final State other = (State) o;
      if ((_cost != other._cost) || !_targetType.equals(other._targetType)) {
        return false;
      }
      if (_nextState == null) {
        // nextStateConverter is null if no state
        return other._nextState == null;
      } else {
        // state converters are from a single list, so test by reference
        return _nextStateConverter == other._nextStateConverter
            && _nextState.equals(other._nextState);
      }
    }

    @Override
    public int hashCode() {
      return _targetType.hashCode();
    }

  }

  private final ConcurrentMap<JavaTypeInfo<?>, List<TypeConverter>> _convertersByTarget = new ConcurrentHashMap<JavaTypeInfo<?>, List<TypeConverter>>();
  private final ConcurrentMap<Class<?>, ConcurrentMap<JavaTypeInfo<?>, Queue<State>>> _validChains = new ConcurrentHashMap<Class<?>, ConcurrentMap<JavaTypeInfo<?>, Queue<State>>>();

  public DefaultValueConverter() {
  }

  protected List<TypeConverter> getConvertersTo(final ValueConversionContext conversionContext, final JavaTypeInfo<?> type) {
    List<TypeConverter> converters = _convertersByTarget.get(type);
    if (converters != null) {
      return converters;
    }
    converters = new ArrayList<TypeConverter>();
    for (TypeConverter converter : conversionContext.getGlobalContext().getTypeConverterProvider().getTypeConverters()) {
      if (converter.canConvertTo(type)) {
        converters.add(converter);
      }
    }
    if (converters.isEmpty()) {
      converters = Collections.<TypeConverter>emptyList();
      _convertersByTarget.putIfAbsent(type, converters);
    } else {
      // Reverse the order so that the most recently added converter is returned first
      Collections.reverse(converters);
      final List<TypeConverter> previous = _convertersByTarget.putIfAbsent(type, converters);
      if (previous != null) {
        converters = previous;
      }
    }
    return converters;
  }

  protected Queue<State> getConversionChains(final Class<?> sourceType, final JavaTypeInfo<?> targetType) {
    ConcurrentMap<JavaTypeInfo<?>, Queue<State>> conversions = _validChains.get(sourceType);
    if (conversions == null) {
      conversions = new ConcurrentHashMap<JavaTypeInfo<?>, Queue<State>>();
      final ConcurrentMap<JavaTypeInfo<?>, Queue<State>> previous = _validChains.putIfAbsent(sourceType, conversions);
      if (previous != null) {
        conversions = previous;
      }
    }
    Queue<State> chains = conversions.get(targetType);
    if (chains == null) {
      chains = new ConcurrentLinkedQueue<State>();
      final Queue<State> previous = conversions.putIfAbsent(targetType, chains);
      if (previous != null) {
        chains = previous;
      }
    }
    return chains;
  }

  private boolean directConversion(final ValueConversionContext conversionContext, final Object value, final JavaTypeInfo<?> type) {
    s_logger.debug("Attempting to convert {} to type {}", value, type);
    if (value == null) {
      if (type.isAllowNull()) {
        s_logger.debug("Type allows NULL");
        return conversionContext.setResult(null);
      } else if (type.isDefaultValue()) {
        s_logger.debug("Type has default value");
        return conversionContext.setResult(type.getDefaultValue());
      } else {
        s_logger.debug("Type does not allow NULL");
        return conversionContext.setFail();
      }
    }
    if (conversionContext.isFailedConversion(value, type)) {
      s_logger.debug("Cached failure of {} to {}", value, type);
      return conversionContext.setFail();
    }
    s_logger.debug("Attempting class assignment from {} to {}", value.getClass(), type.getRawClass());
    if (type.getRawClass().isAssignableFrom(value.getClass())) {
      // TODO: if there are deep cast generic issues, the conversion will need to go deeper (e.g. Foo<X> to Foo<Y> where (? extends X)->Y is a well defined conversion for all values) 
      s_logger.debug("Raw class type is assignable");
      return conversionContext.setResult(value);
    }
    return false;
  }

  private boolean stateConversion(final ValueConversionContext conversionContext, State state) {
    /*{
      final StringBuilder sb = new StringBuilder();
      final Object result = conversionContext.getResult();
      conversionContext.setResult(result);
      sb.append(state.getCost()).append(' ').append(result);
      State s = state;
      do {
        sb.append("--[").append(s.getNextStateConverter()).append("]->");
        s = s.getNextState();
        sb.append(s.getTargetType());
      } while (s.getNextStateConverter() != null);
      System.out.println(sb.toString());
    }*/
    TypeConverter converter = state.getNextStateConverter();
    do {
      state = state.getNextState();
      s_logger.debug("Chained to {}, {}", converter, state);
      converter.convertValue(conversionContext, conversionContext.getResult(), state.getTargetType());
      if (conversionContext.isFailed()) {
        s_logger.debug("Chain failed");
        return false;
      }
      converter = state.getNextStateConverter();
    } while (converter != null);
    s_logger.debug("Chain complete");
    return true;
  }

  @Override
  public void convertValue(final ValueConversionContext conversionContext, Object value, final JavaTypeInfo<?> type) {
    if (type.isAllowNull() || type.isDefaultValue()) {
      if (value instanceof Data) {
        if (DataUtils.isNull((Data) value)) {
          value = null;
        }
      } else if (value instanceof Value) {
        if (ValueUtils.isNull((Value) value)) {
          value = null;
        }
      }
    }
    s_logger.info("Converting {} to type {}", value, type);
    if (directConversion(conversionContext, value, type)) {
      s_logger.debug("Direct conversion complete");
      return;
    }
    final Queue<State> conversionChains = getConversionChains(value.getClass(), type);
    if (!conversionChains.isEmpty()) {
      final Iterator<State> conversions = conversionChains.iterator();
      while (conversions.hasNext()) {
        final State state = conversions.next();
        s_logger.debug("Found cached state-chain {}", state);
        if (type.equals(state.getRootType())) {
          s_logger.debug("Applying conversions");
          if (directConversion(conversionContext, value, state.getTargetType()) && stateConversion(conversionContext, state)) {
            s_logger.debug("Cached conversions successful");
            return;
          } else {
            s_logger.debug("Cached conversions failed");
          }
        } else {
          s_logger.debug("Cached conversions are for different target type");
        }
      }
      // TODO: housekeep to get rid of chains that don't work very often, and promote those that do to the top of the list 
    }
    s_logger.debug("Exploring conversion state space");
    final SortedMap<Integer, Queue<State>> searchStates = new TreeMap<Integer, Queue<State>>();
    State explore = new State(type, null, null, 1);
    s_logger.debug("Processing state {}", explore);
    int statesLoaded = 1;
    int statesStored = 0;
    final Set<JavaTypeInfo<?>> consideredConverters = new HashSet<JavaTypeInfo<?>>();
    do {
      consideredConverters.clear();
      for (TypeConverter converter : getConvertersTo(conversionContext, explore.getTargetType())) {
        if (!explore.visited(converter)) {
          final Map<JavaTypeInfo<?>, Integer> alternativeTypes = converter.getConversionsTo(explore.getTargetType());
          if ((alternativeTypes != null) && !alternativeTypes.isEmpty()) {
            for (Map.Entry<JavaTypeInfo<?>, Integer> alternativeType : alternativeTypes.entrySet()) {
              // Only try types not already used on the chain, and only use the first converter (most recently added) for a type
              if (!explore.visited(alternativeType.getKey()) && consideredConverters.add(alternativeType.getKey())) {
                final State nextState = new State(alternativeType.getKey(), converter, explore, alternativeType.getValue());
                final Integer key = (Integer) nextState.getCost();
                if (key > 100) {
                  // Ignore expensive chains 
                  continue;
                }
                Queue<State> states = searchStates.get(key);
                if (states == null) {
                  states = new LinkedList<State>();
                  searchStates.put(key, states);
                }
                states.add(nextState);
                statesStored++;
              }
            }
          }
        }
      }
      s_logger.debug("{} states processed, {} states queued", statesLoaded, statesStored);
      nextState: do {//CSIGNORE
        if (searchStates.isEmpty()) {
          s_logger.debug("No more states");
          switch (conversionContext.getReentranceCount()) {
            case 0:
              s_logger.warn("Conversion of {} to {} failed", value, type);
              break;
            case 1:
              s_logger.info("Conversion of {} to {} failed", value, type);
              break;
            default:
              s_logger.debug("Conversion of {} to {} failed", value, type);
              break;
          }
          conversionContext.recordFailedConversion(value, type);
          conversionContext.setFail();
          return;
        }
        final Integer key = searchStates.firstKey();
        final Queue<State> states = searchStates.get(key);
        explore = states.remove();
        if (states.isEmpty()) {
          searchStates.remove(key);
        }
        statesLoaded++;
        s_logger.debug("Processing state {}", explore);
        if (directConversion(conversionContext, value, explore.getTargetType()) && !conversionContext.isFailed()) {
          s_logger.debug("Direct conversion possible");
          if (stateConversion(conversionContext, explore)) {
            synchronized (conversionChains) {
              if (!conversionChains.contains(explore)) {
                conversionChains.add(explore);
              }
            }
            return;
          } else {
            continue nextState;
          }
        } else {
          break nextState;
        }
      } while (true);
    } while (true);
  }

}
