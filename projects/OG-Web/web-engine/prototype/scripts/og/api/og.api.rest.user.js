/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.regions',
    dependencies: ['og.api.common', 'og.api.rest'],
    obj: function () {
        var common = og.api.common, api = og.api.rest;
        return { // all requests that begin with /user
            root: 'user',
            get: common.not_implemented_get,
            put: common.not_implemented_put,
            del: common.not_implemented_del,
            login: {
                root: 'user/login',
                get: common.not_implemented_put,
                put: console.log('in'),
                del: common.not_implemented_del
            },
            logout: {
                root: 'user/logout',
                get: common.not_implemented_put,
                put: console.log('out'),
                del: common.not_implemented_del
            }
        };
    }
});