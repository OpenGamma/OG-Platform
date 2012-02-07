/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.Produces;

import com.opengamma.transport.jaxrs.FudgeRest;

/**
 * Abstract base class for RESTful resources.
 */
@Produces(FudgeRest.MEDIA)
public abstract class AbstractDataResource {

}
