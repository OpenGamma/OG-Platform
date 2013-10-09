/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl

/*
 * Adds methods to java.util.Number:
 *   getBp() - converts from basis points, i.e. returns num / 1e4
 *   getPc() - converts from percentage, i.e. returns num / 1e2
 *   getPercent() - same as getPc()
 *
 * This allows users to specify quantities in DSL scripts like "25.bp" or "10.percent".
 * This works because all the methods in the analytics library expect their arguments to be normalized
 */
Number.metaClass.getBp = { -> delegate / 10000 }
Number.metaClass.getPc = { -> delegate / 100 }
Number.metaClass.getPercent = { -> delegate / 100 }
