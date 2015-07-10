/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.legalentities',
    dependencies: ['og.api.common', 'og.api.rest'],
    obj: function () {
        var common = og.api.common, api = og.api.rest;
        return { // all requests that begin with /legalentities
            root: 'legalentities',
            get: api.default_get.partial(['name'], null),
            put: common.not_implemented_put,
            del: common.not_implemented_del
        };
    }
});