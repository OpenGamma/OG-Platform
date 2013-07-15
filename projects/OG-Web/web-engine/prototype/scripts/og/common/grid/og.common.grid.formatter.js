/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.grid.Formatter',
    dependencies: [],
    obj: function () {
        var module = this;
        $.fn.sparkline.defaults.common.disableHiddenCheck = true;
        return function (grid) {
            var formatter = this, curves = false, sparklines = false;
            formatter.CURVE = function (value, width, height) {
                try {
                    curves = true;
                    return '<span class="OG-g-fl" style="width: ' + width + 'px; height: ' + height +
                        'px; position: absolute; top: 0; right: 0; left: 0; bottom: 0">[' +
                        JSON.stringify(value.v) + ']</span>';
                } catch (error) {return og.dev.warn(module.name + ': ', error), '';}
            };
            formatter.DOUBLE = function (value) {
                var curr, last, sparkline, indicator = !value || !value.h || !value.h.length ? null
                    : (curr = value.h[value.h.length - 1]) < (last = value.h[value.h.length - 2]) ? 'down'
                        : curr > last ? 'up' : 'static';
                if (sparkline = grid.config.sparklines && indicator) sparklines = true;
                return !value ? ''
                    : (curr < 0 ? '<span class="og-neg">' + value.v + '</span>' : value.v || '') +
                        (sparkline ? '<span class="OG-g-sp" ' +
                            'style="height: 15px; width: 45px; position: absolute; top: 3px; right: 18px;">[' +
                            JSON.stringify(value.h.map(function (v, i) {return [i, v];})) + ']</span>' : '') +
                        (indicator ? '<span class="OG-icon og-icon-tick-'+ indicator +'"></span>' : '');
            };
            formatter.FUNGIBLE_TRADE = function (value) {return value.v.name;};
            formatter.NODE = function (value) {return value.v.name;};
            formatter.OTC_TRADE = function (value) {return value.v.name;};
            formatter.POSITION = function (value) {return value.v.name;};
            formatter.transform = function (html) {
                var node = $(html);
                // only bother if the grid has some curve values in the last render
                if (curves) (curves = false), node.find('.OG-g-fl').each(function () {
                    var $this = $(this);
                    try {
                        $.plot($this, JSON.parse($this.text()), {
                            grid: {show: false}, colors: ['#456899'],
                            series: {shadowSize: 0, lines: {lineWidth: 1}},
                            xaxis: {show: false}, yaxis: {show: false}
                        });
                    } catch (error) {og.dev.warn(module.name + ': ', error);}
                });
                // only bother if the grid has some sparkline values in the last render
                if (sparklines) (sparklines = false), node.find('.OG-g-sp').each(function () {
                    var $this = $(this);
                    try {
                        $.plot($this, JSON.parse($this.text()), {
                            grid: {show: false}, colors: ['#456899'],
                            series: {shadowSize: 0, lines: {lineWidth: 1}},
                            xaxis: {show: false}, yaxis: {show: false}
                        });
                    } catch (error) {og.dev.warn(module.name + ': ', error);}
                });
                return node;
            };
            formatter.UNKNOWN = function (value, width, height) {
                var type = value && value.t;
                if (!type) return '';
                return value && formatter[type] ? formatter[type](value, width, height) : value && value.v || '';
            };
        };
    }
});
