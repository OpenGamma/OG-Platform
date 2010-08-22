/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.world.Region;
import com.opengamma.engine.world.RegionSource;
import com.opengamma.id.UniqueIdentifier;

/**
 * Builder for converting Region instances to/from Fudge messages.
 */
public class RegionBuilder implements FudgeBuilder<Region> {

  private final FinancialFudgeContextConfiguration _context;

  public RegionBuilder(final FinancialFudgeContextConfiguration context) {
    _context = context;
  }

  private FinancialFudgeContextConfiguration getContext() {
    return _context;
  }

  protected RegionSource getRegionRepository() {
    return getContext().getRegionSource();
  }

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, Region object) {
    return context.objectToFudgeMsg(object.getUniqueIdentifier());
  }

  @Override
  public Region buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    final UniqueIdentifier identifier = context.fudgeMsgToObject(UniqueIdentifier.class, message);
    return getRegionRepository().getRegion(identifier);
  }

}
