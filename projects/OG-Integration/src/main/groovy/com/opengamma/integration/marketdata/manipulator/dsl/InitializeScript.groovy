/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl

import org.threeten.bp.Period

/*
 * Adds methods to java.util.Number:
 *   getBp() - converts from basis points, i.e. returns num / 1e4
 *   getPc() - converts from percentage, i.e. returns num / 1e2
 *   getPercent() - same as getPc()
 * This allows users to specify quantities in DSL scripts like "25.bp" or "10.percent".
 * This works because all the methods in the analytics library expect their arguments to be normalized
 *
 * Also included are date specification methods. The number is converted to a period.
 */
Number.metaClass.getBp = { -> delegate / 10000 }
Number.metaClass.getPc = { -> delegate / 100 }
Number.metaClass.getPercent = { -> delegate / 100 }

Number.metaClass.getD = { -> Period.ofDays(((Number) delegate).intValue()) }
Number.metaClass.getM = { -> Period.ofMonths(((Number) delegate).intValue()) }
Number.metaClass.getY = { -> Period.ofYears(((Number) delegate).intValue()) }
