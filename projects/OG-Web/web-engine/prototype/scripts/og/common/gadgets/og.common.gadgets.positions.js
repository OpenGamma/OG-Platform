/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.gadgets.positions',
    dependencies: [],
    obj: function () {
        var common = og.common, views = og.views, routes = common.routes, api = og.api,
            dependencies = ['id', 'node', 'version'];
        return function (config) {
            /* timeseries */
            var selector = config.selector, get_values, timeseries, view = config.view,
                timeseries_options = {
                    colors: ['#42669a'],
                    series: {shadowSize: 0, threshold: {below: 0, color: '#960505'}},
                    legend: {backgroundColor: null},
                    lines: {lineWidth: 1, fill: 1, fillColor: '#f8fbfd'},
                    grid: {show: false},
                    yaxis: {}
                };
            get_values = function (arr) {
                var min, max, buffer;
                max = (function (arr) {return Math.max.apply(null, arr.pluck(1));})(arr);
                min = (function (arr) {return Math.min.apply(null, arr.pluck(1));})(arr);
                buffer = (max - min) / 10, max += buffer, min -= buffer;
                return {min: min, max: max}
            };
            timeseries = function (obj, height) {
                var $timeseries = $(selector + ' .og-timeseries'),
                    $hover_msg = $(selector + ' .og-timeseries-hover'),
                    $timeseries_extra = $(selector + ' .og-timeseries-extra'),
                    id = obj.data.template_data.hts_id,
                    height_obj = {height: height < 120 ? 120 : height + 'px'};
                if (!id) return $timeseries_extra.html('no timeseries found');
                $timeseries.css(height_obj), $hover_msg.css(height_obj);
                api.rest.timeseries.get({
                    dependencies: dependencies,
                    handler: function (result) {
                        var template_data = result.data.template_data, vals;
                        if (result.error) {
                            common.util.ui.dialog({type: 'error', message: result.message});
                            return $timeseries_extra.html('error loading timeseries data');
                        }
                        vals = get_values(result.data.timeseries.data);
                        timeseries_options.yaxis.min = vals.min;
                        timeseries_options.yaxis.max = vals.max;
                        $.plot($timeseries, [result.data.timeseries.data], timeseries_options);
                        $timeseries_extra
                            .html(template_data.data_field.lang() + ': ' + template_data.data_source.lang());
                        $(selector + ' .og-timeseries-container')
                            .hover(function () {$hover_msg.show();}, function () {$hover_msg.hide();})
                            .click(function (e) {
                                e.preventDefault();
                                routes.go(routes.hash(og.views.timeseries.rules.load_item, {id: id}));
                            });
                    },
                    id: id,
                    cache_for: 500
                });
            };
            api.rest.positions.get({
                dependencies: dependencies,
                handler: function (result) {
                    if (result.error) return alert(result.message);
                    api.text({module: 'og.views.gadgets.positions', handler: function (template) {
                        var args = routes.current().args;
                        $(selector).html($.tmpl(template, $.extend(result.data, {editable: config.editable})))
                            .hide().fadeIn();
                        timeseries(result, $(selector + ' .og-js-sec-time').outerHeight() - 2);
                        if ((!config.version || config.version === '*') && config.editable) {
                            common.util.ui.content_editable({
                                pre_dispatch: function (rest_options, handler) {
                                    og.api.rest.positions.get({
                                        id: config.id,
                                        handler: function (result) {
                                            handler($.extend(rest_options, {
                                                trades: og.common.gadgets.trades.format(result.data.trades)
                                            }));
                                        }
                                    });
                                },
                                handler: function () {view.search(routes.current().args);}
                            });
                        }
                    }});
                },
                id: config.id,
                version: !config.version || config.version === '*' ? (void 0) : config.version,
                cache_for: 500,
                loading: function () {}
            });
        }
    }
});