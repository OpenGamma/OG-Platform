/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Timeseries',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        return function (config) {
            var gadget = this, timeseries,
            alive = og.common.id('gadget_timeseries'), $selector = $(config.selector);
            if (!config.rest_options) $(config.selector).addClass(alive)
                .css({position: 'absolute', top: 0, left: 0, right: 0, bottom: 0});
            var RestDataMan = function (rest_options) {
                var dataman = this;
                og.api.rest.timeseries.get(rest_options).pipe(function (result) {
                    new og.common.gadgets.TimeseriesPlot({
                        selector: config.selector,
                        data: [result.data],
                        rest_options: rest_options,
                        datapoints: !!config.datapoints, height: 400
                    });
                });
            };
            var CellDataMan = function (row, col, type, source) {
                var dataman = this;
                return dataman.cell = new og.analytics
                    .Cell({
                        source: source, col: col, row: row, format: type === 'CURVE' ? 'CELL' : 'EXPANDED'
                    }, 'timeseries')
                    .on('data', function (value) {
                        var data = typeof value.v !== 'undefined' ? value.v : value;
                        if (!timeseries && data && (typeof data === 'object')) timeseries = new og.common.gadgets
                            .TimeseriesPlot($.extend(true, {}, config, {data: [data]}));
                    })
                    .on('fatal', function (message) {$selector.html(message);});

            };
            gadget.alive = function () {
                var live = !!$('.' + alive).length;
                if (!live && timeseries) gadget.dataman.kill();
                return live;
            };
            gadget.resize = function () {try {timeseries.resize();} catch (error) {}};
            gadget.dataman = !!config.rest_options
                ? RestDataMan(config.rest_options)
                : CellDataMan(config.row, config.col, config.type, config.source)
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});