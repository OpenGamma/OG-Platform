/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Formatter',
    dependencies: ['og.analytics.Grid'],
    obj: function () {
        var module = this, flat_sparkline = {
            '0'                                     : null,
            '0,0'                                   : null,
            '0,0,0'                                 : null,
            '0,0,0,0,0'                             : null,
            '0,0,0,0,0,0'                           : null,
            '0,0,0,0,0,0,0'                         : null,
            '0,0,0,0,0,0,0,0'                       : null,
            '0,0,0,0,0,0,0,0,0'                     : null,
            '0,0,0,0,0,0,0,0,0,0'                   : null,
            '0,0,0,0,0,0,0,0,0,0,0'                 : null,
            '0,0,0,0,0,0,0,0,0,0,0,0'               : null,
            '0,0,0,0,0,0,0,0,0,0,0,0,0'             : null,
            '0,0,0,0,0,0,0,0,0,0,0,0,0,0'           : null,
            '0,0,0,0,0,0,0,0,0,0,0,0,0,0,0'         : null,
            '0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0'       : null,
            '0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0'   : null,
            '0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0' : null
        }, flat_image = '<span class="fsp"><img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADwAAAAOCAYAAABzTn' +
            '/UAAAAWElEQVRIie3SwQmAQAxE0dmO7MGbLaSgbCHWsaX5vYgH8ei6oPMgEJhDEohkZmZmZs9AKqSCVCCV0ft0RyqogipIxRnMC02Co7' +
            'aX+u5zYlq5PfirfvfSVztkN4k8DWtiVAAAAABJRU5ErkJggg==" /></span>';
        var flat = function (data, str) {
            return (str in flat_sparkline) || data.reduce(function (acc, val) {
                if (!acc.val) return acc;
                acc.val = acc.last === val;
                acc.last = val;
                return acc;
            }, {last: data[0], val: true}).val;
        };
        $.fn.sparkline.defaults.common.disableHiddenCheck = true;
        return function (grid) {
            var formatter = this;
            formatter.DOUBLE = function (value) {
                var data;
                return value ? (value.v || '') +
                    (grid.sparklines ? (flat(value.h, (str = value.h.join(',')))
                        ? flat_image : '<span class="sp" values="' + str + '"></span>') : '')
                    : '';
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
