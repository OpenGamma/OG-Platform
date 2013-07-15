/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.livedatasources',
    dependencies: ['og.api.common', 'og.api.rest'],
    obj: function () {
        var common = og.api.common, api = og.api.rest;
        return { // all requests that begin with /livedatasources
            root: 'livedatasources',
            get: api.simple_get,
            put: common.not_available_put,
            del: common.not_available_del
        };
    }
});