/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 *
 * Clone an object by value
 */
Object.clone = function (obj) {return JSON.parse(JSON.stringify(obj));};