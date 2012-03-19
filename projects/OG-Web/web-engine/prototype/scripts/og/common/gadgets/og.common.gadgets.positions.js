/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.gadgets.positions',
    dependencies: [],
    obj: function () {
        var common = og.common, views = og.views, routes = common.routes, api = og.api,
            format_trades = og.common.gadgets.trades.format,
            dependencies = ['id', 'node', 'version'];
        return function (config) {
            /* timeseries */
            var selector = config.selector, get_values, timeseries, view = config.view,
                editable = config.editable && !og.app.READ_ONLY, rest_options,
                timeseries_options = {
                    colors: ['#42669a'],
                    series: {shadowSize: 0, threshold: {below: 0, color: '#960505'}},
                    legend: {backgroundColor: null},
                    lines: {lineWidth: 1, fill: 1, fillColor: '#f8fbfd'},
                    grid: {show: false},
                    yaxis: {}
                };
            get_values = function (arr) {
                var values = arr.pluck(1), max, min,
                    buffer = ((max = Math.max.apply(null, values)) - (min = Math.min.apply(null, values))) / 10;
                return {min: min - buffer, max: max + buffer};
            };
            timeseries = function (obj, height) {
                var $timeseries = $(selector + ' .og-timeseries'),
                    $hover_msg = $(selector + ' .og-timeseries-hover'),
                    $timeseries_extra = $(selector + ' .og-timeseries-extra'),
                    id = obj.data.template_data.hts_id,
                    height_obj = {height: height < 120 ? 120 : height + 'px'};
                if (!id) return $timeseries_extra.html('no timeseries found');
                $timeseries.css(height_obj), $hover_msg.css(height_obj);
                api.rest.timeseries.get({dependencies: dependencies, id: id, cache_for: 500}).pipe(function (result) {
                    var template_data = result.data.template_data;
                    if (result.error) {
                        view.error({type: 'error', message: result.message});
                        return $timeseries_extra.html('error loading timeseries data');
                    }
                    timeseries_options.yaxis = get_values(result.data.timeseries.data);
                    $.plot($timeseries, [result.data.timeseries.data], timeseries_options);
                    $timeseries_extra.html(template_data.data_field.lang() + ': ' + template_data.data_source.lang());
                    $(selector + ' .og-timeseries-container')
                        .hover(function () {$hover_msg.show();}, function () {$hover_msg.hide();})
                        .click(function (e) {
                            e.preventDefault();
                            routes.go(routes.hash(og.views.timeseries.rules.load_item, {id: id}));
                        });
                });
            };
            rest_options = {
                dependencies: dependencies, id: config.id, cache_for: 500,
                version: !config.version || config.version === '*' ? (void 0) : config.version
            };
            $.when(api.rest.positions.get(rest_options), api.text({module: 'og.views.gadgets.positions'}))
                .then(function (result, template) {
                    if (result.error) return view.error(result.message);
                    var $html = $.tmpl(template, $.extend(result.data, {editable: editable})),
                        cur_page = routes.current().page.substring(1),
                        url = '#/positions/' + result.data.template_data.object_id,
                        link = function () {return '<a href="'+ url +'">' + $(this).text() + '</a>'};
                        if (cur_page !==  'positions') $html.find('thead span').html(link);
                    $(selector).html($html).hide().fadeIn();
                    timeseries(result, $(selector + ' .og-js-sec-time').outerHeight() - 2);
                    if (editable) common.util.ui.content_editable({
                        pre_dispatch: function (rest_options, handler) {
                            og.api.rest.positions.get({id: config.id}).pipe(function (result) {
                                handler($.extend(rest_options, {trades: format_trades(result.data.trades)}));
                            });
                        },
                        handler: function () {view.search(routes.current().args);}
                    });
                });
        }
    }
});