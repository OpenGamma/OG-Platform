/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch.rest;


import com.opengamma.util.ArgumentChecker;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * Abstract REST resource wrapper for a Underlying.
 */
public abstract class AbstractResource<Underlying> {
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * The underlying data master.
   */
  private final Underlying _underlying;


  public AbstractResource(FudgeContext fudgeContext, Underlying underlying) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(underlying, "underlying");
    _fudgeContext = fudgeContext;
    _underlying = underlying;
  }

  /**
   * Returns the Fudge context.
   *
   * @return the context
   */
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Creates and returns a Fudge serializer based on the Fudge context.
   *
   * @return the serializer
   */
  protected FudgeSerializer getFudgeSerializer() {
    return new FudgeSerializer(getFudgeContext());
  }

  /**
   * Creates and returns a Fudge deserializer based on the Fudge context.
   *
   * @return the deserializer
   */
  protected FudgeDeserializer getFudgeDeserializer() {
    return new FudgeDeserializer(getFudgeContext());
  }

  /**
   * Returns the underlying master.
   *
   * @return the master
   */
  protected Underlying getUnderlying() {
    return _underlying;
  }

}
