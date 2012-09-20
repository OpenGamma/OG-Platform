/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.url',
    dependencies: ['og.api.rest'],
    obj: function () {
        var current = {}, panels = ['one', 'two', 'three', 'four'],
            layout = {one: 'south', two: 'dock-north', three: 'dock-center', four: 'dock-south'};
        return {
            add: function (container, params) {
                // add a gadget to the URL
            },
            process: function (args) {
                // process the arguments from og.common.routes
            },
            remove: function (container, index) {
                // remove a gadget from the URL
            }
        };
    }
});