/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */

String.prototype.lang = function (bool) {
    return (og && og.common && og.common.lang) ? og.common.lang(this, bool) : this;
};