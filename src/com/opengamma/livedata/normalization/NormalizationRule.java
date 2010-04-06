/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.io.Serializable;

import org.fudgemsg.MutableFudgeFieldContainer;

/**
 * 
 *
 * @author pietari
 */
public interface NormalizationRule extends Serializable {
  
  public void apply(MutableFudgeFieldContainer msg);

}
