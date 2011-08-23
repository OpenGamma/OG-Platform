/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.ArgumentChecker;

/**
 * Decorates a {@link Value} object with language specific metadata.
 * 
 * @param <Decoration> class containing the decoration metadata
 */
public abstract class ValueDecorator<Decoration extends ValueDecoration> {

  /**
   * A decorated value instance.
   */
  private static final class DecoratedValue<Decoration> extends Value {

    private static final long serialVersionUID = 1L;

    private final Decoration _decoration;

    private DecoratedValue(final Value copyFrom, final Decoration decoration) {
      super(copyFrom);
      _decoration = decoration;
    }

    public Decoration getDecoration() {
      return _decoration;
    }

    @Override
    public Value clone() {
      return new DecoratedValue<Decoration>(this, getDecoration());
    }

    @Override
    public void toFudgeMsg(final FudgeSerializer serializer, final MutableFudgeMsg msg) {
      super.toFudgeMsg(serializer, msg);
      // The containing DATA will have put class header information in. Remove this.
      msg.remove(0);
    }

    @Override
    public String toString() {
      return super.toString() + "(" + getDecoration().toString() + ")";
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof DecoratedValue<?>)) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }
      final DecoratedValue<?> other = (DecoratedValue<?>) o;
      return ObjectUtils.equals(getDecoration(), other.getDecoration());
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

  }

  @SuppressWarnings("unchecked")
  public Decoration get(final Value value) {
    ArgumentChecker.notNull(value, "value");
    if (value instanceof DecoratedValue) {
      return ((DecoratedValue<Decoration>) value).getDecoration();
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public Value applyTo(final Decoration decoration, final Value value) {
    ArgumentChecker.notNull(value, "value");
    if (value instanceof DecoratedValue) {
      if (((DecoratedValue<Decoration>) value).getDecoration() == decoration) {
        return value;
      }
    }
    return new DecoratedValue<Decoration>(value, decoration);
  }

  public abstract Decoration create();

}
