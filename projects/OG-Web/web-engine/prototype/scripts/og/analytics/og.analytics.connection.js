/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.connection',
    dependencies: ['og.api.rest'],
    obj: function () {
        var connection;
        var data_handler = function (result) {};
        var deliver = function (data) {};
        return connection = {
            deregister: function (dataman) {},
            register: function (dataman) {}
        };
    }
});