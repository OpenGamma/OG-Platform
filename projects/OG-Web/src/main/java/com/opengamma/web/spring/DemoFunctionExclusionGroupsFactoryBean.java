/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.exclusion.AbstractFunctionExclusionGroups;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroup;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Creates FunctionExclusionGroups appropriate for the {@link DemoStandardFunctionConfiguration} functions.
 * <p>
 * The implementation created will look for the {@link OpenGammaFunctionExclusions} marker interface and use the returned string as a key. Keys are ordered with a numbering from large positive
 * integers downwards. If the key string starts with a number, that number is used, otherwise they are given a unique negative number.
 */
public class DemoFunctionExclusionGroupsFactoryBean extends SingletonFactoryBean<FunctionExclusionGroups> {

  @Override
  protected FunctionExclusionGroups createObject() {
    return new AbstractFunctionExclusionGroups() {

      private final AtomicInteger _nextIdentifier = new AtomicInteger();
      private final ConcurrentMap<Integer, Object> _used = new ConcurrentHashMap<Integer, Object>();

      private boolean notUsed(final Integer intValue, final Object key) {
        final Object existing = _used.putIfAbsent(intValue, key);
        if (existing == null) {
          return true;
        }
        return existing.equals(key);
      }

      @Override
      protected String getKey(final FunctionDefinition function) {
        if (function instanceof OpenGammaFunctionExclusions) {
          return ((OpenGammaFunctionExclusions) function).getMutualExclusionGroup();
        } else {
          return null;
        }
      }

      @Override
      protected FunctionExclusionGroup createExclusionGroup(Object key, String displayName) {
        final String keyString = key.toString();
        Integer keyInteger;
        do {
          if (keyString.length() > 0) {
            char c = keyString.charAt(0);
            if (Character.isDigit(c)) {
              int i = c - '0';
              int j;
              for (j = 1; j < keyString.length(); j++) {
                c = keyString.charAt(j);
                if (!Character.isDigit(c)) {
                  break;
                }
                i = (i * 10) + (c - '0');
              }
              keyInteger = i;
              displayName = displayName.substring(j);
              assert notUsed(keyInteger, key);
              break;
            }
          }
          keyInteger = _nextIdentifier.decrementAndGet();
        } while (false);
        return super.createExclusionGroup(keyInteger, displayName);
      }

      @Override
      public boolean isExcluded(final FunctionExclusionGroup group, final Collection<FunctionExclusionGroup> existing) {
        final int groupKey = (Integer) getKey(group);
        for (FunctionExclusionGroup toCheck : existing) {
          final Integer toCheckKey = (Integer) getKey(toCheck);
          if (groupKey >= toCheckKey.intValue()) {
            return true;
          }
        }
        return false;
      }

      @Override
      public Collection<FunctionExclusionGroup> withExclusion(final Collection<FunctionExclusionGroup> existing, final FunctionExclusionGroup newGroup) {
        final List<FunctionExclusionGroup> result = new ArrayList<FunctionExclusionGroup>(existing.size());
        result.addAll(existing);
        result.add(newGroup);
        return result;
      }

    };
  }
}
