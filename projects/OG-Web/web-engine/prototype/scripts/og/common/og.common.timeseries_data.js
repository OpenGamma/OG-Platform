/**
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.TimeseriesData',
    dependencies: ['og.api.rest'],
    obj: function () {
        var api = og.api;
        return function (config) {
            var timeseries_data = this, template, selector = config.selector, colors_arr = config.colors;
            timeseries_data.update = function (data) {
                var col_width, render_grid, grid_width, $selector = $(selector);
                if (!$selector.length) return;
                $selector.html('<div class="og-container"></div>');
                (function () { // Calculate data grids "grid_width" and "col_width"
                    var num_cols = data.length * 2, default_col = 180,
                        min_width = num_cols * default_col,
                        container_width = $selector.width();
                    if (min_width < container_width) col_width = container_width / num_cols;
                    else col_width = default_col;
                    grid_width = ~~(2 * col_width);
                }());
                if (!data) return $selector.html('<span class="og-no-datapoint">No data available</span>');
                render_grid = function (index) {
                    var data_selector = selector + ' .og-data-' + index;
                    new og.common.gadgets.Data({
                        resource: 'timeseries', rest_options: {id: data[index].object_id},
                        type: 'TIME_SERIES', selector: data_selector, menu: false, child: false
                    });
                };
                data.forEach(function (v, i) {
                    var cur_id = data[i].object_id, $compiled = $((Handlebars.compile(template))({
                        index: i, color: colors_arr[i],
                        source: data[i].data_source, provider: data[i].data_provider
                    }));
                    $compiled.find('.og-js-timeseries-csv').attr({
                        'href': '/jax/timeseries/' + cur_id + '.csv',
                        'download': 'Timeseries-' + cur_id + '.csv'
                    }).end().appendTo($selector);
                    setTimeout(render_grid.partial(i), 0);
                });
                $selector.find('.og-data-series').css({width: grid_width + 'px', position: 'relative'});
            };
            $.when(og.api.text({module: 'og.views.gadgets.timeseries.data_tash'})).then(function (tmpl) {
                template = tmpl, data = config.data[0], template_data = data.template_data;
                timeseries_data.update([{
                    data_provider: template_data.data_provider,
                    data_source: template_data.data_source,
                    label: template_data.name,
                    object_id: template_data.object_id
                }]);
            })
        };
    }
});