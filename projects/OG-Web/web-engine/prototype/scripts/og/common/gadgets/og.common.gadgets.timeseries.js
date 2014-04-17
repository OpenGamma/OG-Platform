/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Timeseries',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        return function (config) {
            var api = og.api, common = og.common, gadget = this, timeseries, selector = config.selector,
                alive = common.id('gadget_timeseries'), $selector = $(selector),
                colors_arr = ['#42669a', '#ff9c00', '#00e13a', '#313b44'];
            if (!config.rest_options) {
                $(selector).addClass(alive).css({position: 'absolute', top: 0, left: 0, right: 0, bottom: 0});
            }
            var RestDataMan = function (rest_options) {
                var dataman = this;
                $.when(og.api.text({module: 'og.views.gadgets.timeseries'})).then(function (tmpl) {
                    $selector.html(tmpl);
                    og.api.rest.timeseries.get(rest_options).pipe(function (result) {
                        timeseries = new common.Timeseries({
                            selector: selector + ' .og-timeseries',
                            data: [result.data],
                            rest_options: rest_options,
                            datapoints: !!config.datapoints,
                            height: 400
                        });
                        timeseries.datapoints = new common.TimeseriesData({
                            selector: selector + ' .og-data',
                            data: [result.data],
                            rest_options: rest_options,
                            colors: colors_arr
                        });
                        common.TimeseriesMenu.call(timeseries, result, selector + ' .og-menu', colors_arr);
                    });
                });
            };
            var CellDataMan = function (row, col, type, source) {
                var dataman = this, format = type === 'CURVE' ? 'CELL' : 'EXPANDED';
                return dataman.cell = new og.analytics
                    .Cells({source: source, single: {row: row, col: col}, format: format}, 'timeseries')
                    .on('data', function (value) {
                        gadget.data = typeof value.v !== 'undefined' ? value.v : value;
                        if (!timeseries && gadget.data && (typeof gadget.data === 'object')) {
                            timeseries = new common.Timeseries($.extend(true, {}, config,
                                {data: [gadget.data]}, {update: update}));
                        } else if (timeseries) {
                            timeseries.display_refresh();
                        }
                    })
                    .on('fatal', function (message) {$selector.html(message); });
            };
            var update = function () {
                return gadget.data;
            };
            gadget.alive = function () {
                var live = !!$('.' + alive).length;
                if (!live && timeseries) {
                    gadget.dataman.kill();
                }
                return live;
            };
            gadget.resize = function () {
                try {
                    timeseries.resize();
                } catch (error) {/*do nothing*/}
            };
            gadget.dataman = !!config.rest_options
                ? RestDataMan(config.rest_options)
                : CellDataMan(config.row, config.col, config.type, config.source)
            if (!config.child) {
                common.gadgets.manager.register(gadget);
            }
        };
    }
});