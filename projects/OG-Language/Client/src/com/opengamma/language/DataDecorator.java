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
 * Decorates a {@link Data} object with language specific metadata.
 * 
 * @param <Decoration> class containing the decoration metadata
 */
public abstract class DataDecorator<Decoration extends DataDecoration> {

  /**
   * A decorated data instance.
   */
  private static final class DecoratedData<Decoration> extends Data {

    private static final long serialVersionUID = 1L;

    private final Decoration _decoration;

    private DecoratedData(final Data copyFrom, final Decoration decoration) {
      super(copyFrom);
      _decoration = decoration;
    }

    public Decoration getDecoration() {
      return _decoration;
    }

    @Override
    public Data clone() {
      return new DecoratedData<Decoration>(this, getDecoration());
    }

    @Override
    public void toFudgeMsg(final FudgeSerializer serializer, final MutableFudgeMsg msg) {
      super.toFudgeMsg(serializer, msg);
      // The containing RESULT will have put class header information in. Remove this.
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
      if (!(o instanceof DecoratedData<?>)) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }
      final DecoratedData<?> other = (DecoratedData<?>) o;
      return ObjectUtils.equals(getDecoration(), other.getDecoration());
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

  }

  @SuppressWarnings("unchecked")
  public Decoration get(final Data data) {
    ArgumentChecker.notNull(data, "data");
    if (data instanceof DecoratedData) {
      return ((DecoratedData<Decoration>) data).getDecoration();
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public Data applyTo(final Decoration decoration, final Data data) {
    ArgumentChecker.notNull(data, "data");
    if (data instanceof DecoratedData) {
      if (((DecoratedData<Decoration>) data).getDecoration() == decoration) {
        return data;
      }
    }
    return new DecoratedData<Decoration>(data, decoration);
  }

  public abstract Decoration create();

}
