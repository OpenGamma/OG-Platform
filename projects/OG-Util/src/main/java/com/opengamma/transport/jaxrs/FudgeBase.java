/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import org.fudgemsg.FudgeContext;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Base class for the Fudge JAX-RS objects.
 */
/* package */abstract class FudgeBase {

  /**
   * The Fudge context.
   */
  private FudgeContext _fudgeContext;
  /**
   * The Fudge taxonomy.
   */
  private int _fudgeTaxonomyId;

  /**
   * Creates an instance.
   */
  protected FudgeBase() {
    this(OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates an instance.
   * 
   * @param fudgeContext the Fudge context to use
   */
  protected FudgeBase(final FudgeContext context) {
    setFudgeContext(context);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Fudge context.
   * 
   * @return the context, not null
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Sets the Fudge context.
   * 
   * @param fudgeContext the context to use, not null
   */
  public void setFudgeContext(final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the taxonomy id.
   * 
   * @return the taxonomy id
   */
  public int getFudgeTaxonomyId() {
    return _fudgeTaxonomyId;
  }

  /**
   * Sets the taxonomy id.
   * 
   * @param fudgeTaxonomyId the taxonomy id, which must be a 16-bit signed integer
   */
  public void setFudgeTaxonomyId(final int fudgeTaxonomyId) {
    if (fudgeTaxonomyId < Short.MIN_VALUE || fudgeTaxonomyId > Short.MAX_VALUE) {
      throw new IllegalArgumentException("fudgeTaxonomyId must be 16-bit signed integer");
    }
    _fudgeTaxonomyId = fudgeTaxonomyId;
  }

}
