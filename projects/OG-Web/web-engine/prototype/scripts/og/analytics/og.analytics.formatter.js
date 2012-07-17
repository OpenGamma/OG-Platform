/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Formatter',
    dependencies: ['og.analytics.Grid'],
    obj: function () {
        var module = this;
        return function (grid) {
            var formatter = this;
            formatter.DOUBLE = function (value) {
                return value ? (value.v || '') + '<span class="sp" values="' + value.h.join(',') + '"></span>' : '';
            };
            grid.on('render', function () {grid.elements.parent.find('.OG-g .sp').sparkline();});
        };
    }
});