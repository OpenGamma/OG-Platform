/*
 * Copyright 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.positions',
    dependencies: [],
    obj: function () {
        var dependencies = ['id', 'node', 'version'],
            common = og.common, views = og.views, routes = common.routes, api = og.api,
            format_trades = og.common.gadgets.trades.format;
        /**
         * Calculate Timeseries Y axis
         * @param {Array} arr Timeseries data
         * @return {Object} Flot Y axis options
         */
        var get_values = function (arr) {
            var values = arr.pluck(1), max, min,
                buffer = ((max = Math.max.apply(null, values)) - (min = Math.min.apply(null, values))) / 10;
            return {min: min - buffer, max: max + buffer};
        };
        /**
         * Generate link
         * @param {Boolean} external_links
         * @param {Object} result positions rest response
         * @return {String}
         */
        var link = function (external_links, result) {
            if (external_links) return $(this).html();
            var url = routes.prefix() + routes.hash(og.views.positions.rules.load_item, {
                id: result.data.template_data.object_id
            });
            return '<a href="' + url + '">' + $(this).text() + '</a>';
        };
        return function (config) {
            var gadget = this, alive = og.common.id('gadget_position'),
                current_page = routes.current().page.substring(1);
            gadget.alive = function () {return $('.' + alive).length ? true : false;};
            gadget.resize = function () {gadget.load();};
            gadget.load = function () {
                var selector = config.selector, view = config.view,  editable = config.editable && !og.app.READ_ONLY,
                    external_links = 'external_links' in config ? config.external_links : false,
                    template_options = {editable: editable, external_links: external_links, child: config.child};
                var timeseries = function (obj) {
                    var $container = $(selector + ' .og-timeseries-container'),
                        $timeseries = $(selector + ' .og-timeseries'),
                        $hover_msg = $(selector + ' .og-timeseries-hover'),
                        $timeseries_extra = $(selector + ' .og-timeseries-extra'),
                        id = obj.data.template_data.hts_id,
                        height_obj = {height: '50px', width: $container.width()},
                        timeseries_options = {
                            colors: ['#42669a'],
                            series: {shadowSize: 0, threshold: {below: 0, color: '#960505'}},
                            legend: {backgroundColor: null},
                            lines: {lineWidth: 1, fill: 1, fillColor: '#f8fbfd'},
                            grid: {show: false},
                            yaxis: {}
                        };
                    if (!id) return $timeseries_extra.html('no timeseries found');
                    $timeseries.css(height_obj), $hover_msg.css(height_obj);
                    api.rest.timeseries.get({dependencies: dependencies, id: id, cache_for: 500})
                        .pipe(function (result) {
                            var template_data = result.data.template_data;
                            if (result.error) {
                                if (view) view.error({type: 'error', message: result.message});
                                return $timeseries_extra.html('error loading timeseries data');
                            }
                            timeseries_options.yaxis = get_values(result.data.timeseries.data);
                            $.plot($timeseries, [result.data.timeseries.data], timeseries_options);
                            $timeseries_extra
                                .html(template_data.data_field.lang() + ': ' + template_data.data_source.lang());
                            if (!external_links) $container
                                .hover(function () {$hover_msg.show();}, function () {$hover_msg.hide();})
                                .click(function (e) {
                                    e.preventDefault();
                                    routes.go(routes.hash(og.views.timeseries.rules.load_item, {id: id}));
                                });
                    });
                };
                $.when(
                    api.rest.positions.get({
                        dependencies: dependencies, id: config.id, cache_for: 500,
                        version: !config.version || config.version === '*' ? (void 0) : config.version
                    }),
                    api.text({module: 'og.views.gadgets.positions'}))
                .then(function (result, template) {
                    if (result.error) return view ? view.error(result.message)
                        : $(selector).addClass(alive).html(result.message);
                    var $html = $.tmpl(template, $.extend(result.data, template_options));
                    if (current_page !==  'positions') $html.find('thead span').html(link(external_links, result));
                    $(selector).addClass(alive).html($html);
                    timeseries(result);
                    if (editable) common.util.ui.content_editable({
                        pre_dispatch: function (rest_options, handler) {
                            og.api.rest.positions.get({id: config.id}).pipe(function (result) {
                                handler($.extend(rest_options, {trades: format_trades(result.data.trades)}));
                            });
                        },
                        handler: function () {if (view) view.search(routes.current().args);}
                    });
                    if (!config.child) og.common.gadgets.manager.register(gadget);
                });
            };
            gadget.load();
        };
    }
});