/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.regions',
    dependencies: ['og.api.common', 'og.api.rest'],
    obj: function () {
        var common = og.api.common, api = og.api.rest;
        return { // all requests that begin with /regions
            root: 'regions',
            get: api.default_get.partial(['name', 'classification'], null),
            put: common.not_implemented_put,
            del: common.not_implemented_del
        };
    }
});