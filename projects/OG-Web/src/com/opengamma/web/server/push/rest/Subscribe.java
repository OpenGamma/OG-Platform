/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import com.opengamma.id.UniqueId;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation for REST methods that return an entity with a {@link UniqueId}.  When the method is called a
 * subscription is created for the calling client.  When the entity changes the client will be notified.
 * The notification will contain the REST URL used to invoke the method.  This is to allow clients to re-request
 * stale data.
 */@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe {
}
