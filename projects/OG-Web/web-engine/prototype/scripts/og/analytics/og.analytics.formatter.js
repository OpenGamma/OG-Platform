/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Formatter',
    dependencies: ['og.analytics.Grid'],
    obj: function () {
        var module = this;
        $.fn.sparkline.defaults.common.disableHiddenCheck = true;
        return function (grid) {
            var formatter = this;
            formatter.DOUBLE = function (value) {
                return !value ? ''
                    : (value.v || '') +
                        (grid.sparklines ? '<span class="sp" values="' + value.h.join(',') + '"></span>' : '');
            };
            formatter.UNKNOWN = function (value) {
                var type = value.t; delete value.t;
                return value && formatter[type] ? formatter[type](value) : value && value.v || '';
            };
            grid.on('render', function () {
                var options = {
                    type: 'line', lineColor: '#b0b0b0', fillColor: '#ecedee', spotColor: '#b0b0b0',
                    minSpotColor: '#b0b0b0', maxSpotColor: '#b0b0b0', disableInteraction: true
                };
                grid.elements.parent.find('.OG-g .sp').sparkline('html', options);
            });
        };
    }
});
