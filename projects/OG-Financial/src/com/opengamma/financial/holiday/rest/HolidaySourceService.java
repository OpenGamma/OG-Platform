/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.holiday.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.util.rest.AbstractResourceService;

/**
 * RESTful backend for {@link RemoteHolidaySource}.
 */
@Path("holidaySource")
public class HolidaySourceService extends AbstractResourceService<HolidaySource, HolidaySourceResource> {

  public HolidaySourceService(final FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected HolidaySourceResource createResource(HolidaySource underlying) {
    return new HolidaySourceResource(underlying, getFudgeContext());
  }

}
