/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeObjectBuilder;

/**
 * 
 *
 * @author pietari
 */
public class LiveDataSpecificationImplBuilder implements FudgeObjectBuilder<LiveDataSpecification> {

  @Override
  public LiveDataSpecification buildObject(
      FudgeDeserializationContext context, FudgeFieldContainer message) {
    return new LiveDataSpecificationImpl(message);
  }

}
