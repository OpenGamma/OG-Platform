/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Default implementation of the {@link ComputationTargetTypeProvider} populated from the public constants on {@ComputationTargetType}.
 */
public final class DefaultComputationTargetTypeProvider implements ComputationTargetTypeProvider {

  private final Collection<ComputationTargetType> _simpleTypes;
  private final Collection<ComputationTargetType> _additionalTypes;
  private final Collection<ComputationTargetType> _allTypes;

  public DefaultComputationTargetTypeProvider() {
    final List<ComputationTargetType> simpleTypes = new ArrayList<ComputationTargetType>();
    final List<ComputationTargetType> additionalTypes = new ArrayList<ComputationTargetType>();
    final ComputationTargetTypeVisitor<Void, Boolean> isSimple = new ComputationTargetTypeVisitor<Void, Boolean>() {

      @Override
      public Boolean visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final Void data) {
        return Boolean.FALSE;
      }

      @Override
      public Boolean visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final Void data) {
        return Boolean.FALSE;
      }

      @Override
      public Boolean visitNullComputationTargetType(final Void data) {
        return null;
      }

      @Override
      public Boolean visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final Void data) {
        if (UniqueIdentifiable.class.equals(type)) {
          return null;
        } else {
          return Boolean.TRUE;
        }
      }

    };
    try {
      final Class<?> c = ComputationTargetType.class;
      for (Field field : c.getDeclaredFields()) {
        if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) &&
            field.isSynthetic() == false && c.isAssignableFrom(field.getType())) {
          final ComputationTargetType type = (ComputationTargetType) field.get(null);
          final Boolean simple = type.accept(isSimple, null);
          if (simple != null) {
            if (simple.booleanValue()) {
              simpleTypes.add(type);
            } else {
              additionalTypes.add(type);
            }
          }
        }
      }
    } catch (IllegalAccessException e) {
      throw new OpenGammaRuntimeException("Can't initialise", e);
    }
    _simpleTypes = Collections.unmodifiableList(simpleTypes);
    _additionalTypes = Collections.unmodifiableList(additionalTypes);
    final List<ComputationTargetType> allTypes = new ArrayList<ComputationTargetType>(simpleTypes);
    allTypes.addAll(additionalTypes);
    _allTypes = Collections.unmodifiableList(allTypes);
  }

  @Override
  public Collection<ComputationTargetType> getSimpleTypes() {
    return _simpleTypes;
  }

  @Override
  public Collection<ComputationTargetType> getAdditionalTypes() {
    return _additionalTypes;
  }

  @Override
  public Collection<ComputationTargetType> getAllTypes() {
    return _allTypes;
  }

}
