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
                var curr, last, indicator = !value || !value.h || !value.h.length ? null
                    : (curr = value.h[value.h.length - 1]) < (last = value.h[value.h.length - 2]) ? 'down'
                        : curr > last ? 'up' : 'static';
                return !value ? ''
                    : (value.v || '') +
                        (grid.config.sparklines ? '<span class="sp" values="' + value.h.join(',') + '"></span>' : '') +
                        (indicator ? '<span class="OG-icon og-icon-tick-'+ indicator +'"></span>' : '');
            };
            formatter.UNKNOWN = function (value) {
                var type = value.t;
                return value && formatter[type] ? formatter[type](value) : value && value.v || '';
            };
            formatter.LABELLED_MATRIX_1D = function (value) {
                return value + '<span class="og-js-menu-hook"></span>';
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
