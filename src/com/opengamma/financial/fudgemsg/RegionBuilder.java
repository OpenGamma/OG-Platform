/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.Region;
import com.opengamma.financial.RegionRepository;
import com.opengamma.id.UniqueIdentifier;

/**
 * Builder for converting Region instances to/from Fudge messages.
 */
@FudgeBuilderFor(Region.class)
public class RegionBuilder implements FudgeBuilder<Region> {

  private final FinancialFudgeContextConfiguration _context;

  public RegionBuilder(final FinancialFudgeContextConfiguration context) {
    _context = context;
  }

  private FinancialFudgeContextConfiguration getContext() {
    return _context;
  }

  protected RegionRepository getRegionRepository() {
    return getContext().getRegionRepository();
  }

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, Region object) {
    return context.objectToFudgeMsg(object.getUniqueIdentifier());
  }

  @Override
  public Region buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    final UniqueIdentifier identifier = context.fudgeMsgToObject(UniqueIdentifier.class, message);
    return getRegionRepository().getHierarchyNode(LocalDate.now(Clock.system(TimeZone.UTC)), identifier);
  }

}
