/**
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 *
 * Renders a Canvas plot using Flot
 * @see http://code.google.com/p/flot/
 */
$.register_module({
    name: 'og.common.gadgets.TimeseriesPlot',
    dependencies: ['og.api.rest', 'og.common.gadgets.manager'],
    obj: function () {
        var api = og.api, prefix = 'timeseries_', counter = 1;
        /**
         * @param {object} config
         * @param {String} config.selector
         * @param {String} config.id
         * @param {String} config.height optional height of Timeseries, gadget will calculate height from parent
         * if not supplied
         * @param {Boolean} config.datapoints
         * @param {Boolean} config.datapoints_link
         * @param {Boolean} config.child Manage resizing gadget manualy
         * @param {Object} config.data Spoffed Data - temporary solution
         */
        return function (config) {
            var timeseries = this, handler, x_max, alive = prefix + counter++, selector = config.selector,
                load_plots, initial_preset, meta = {}, // object that stores the structure and data of the plots
                plot_template, data_template, common_plot_options, top_plot_options, bot_plot_options, spoofed_data,
                colors_arr = ['#42669a', '#ff9c00', '#00e13a', '#313b44'], // line colors for plot 1 data sets
                colors_arr_p2 = ['#fff', '#fff', '#fff', '#fff']; // line colors for plot 2 data sets
            timeseries.resize = (function (timeout) {
               var resize = function () {
                   var height = config.height ? config.height : $(selector).parent().height(),
                       width = $(selector).width(),
                       h_ticks = Math.ceil(width / 100),
                       v_ticks = Math.ceil((height - 80) / 50);
                   $(selector).find('.og-js-p1, .og-js-p2, .og-flot-xaxis').width(width - 2 + 'px');
                   $(selector).find('.og-js-p1').height(height - 83);
                   top_plot_options.xaxis.ticks = bot_plot_options.xaxis.ticks = h_ticks;
                   top_plot_options.yaxis.ticks = bot_plot_options.yaxis.ticks = v_ticks;
                   load_plots();
               };
               return function () {timeout = clearTimeout(timeout) || setTimeout(resize, 0);}
            })(null);
            timeseries.alive = function () {return !!$('.' + alive).length;};
            spoofed_data = (function (data) {
                if (!data) return null;
                data.forEach(function (val, idx) {
                    var field = val.template_data.data_field, time = val.template_data.observation_time;
                    if (!meta[field]) meta[field] = {};
                    meta[field][time] = {error: false, data: data[idx]};
                });
                return {main: {error: false, data: data[0]}, search: {error: false, data: {data: []}}};
            })(config.data);
            common_plot_options = {
                grid: {borderWidth: 0, color: '#999', aboveData: false, minBorderMargin: 0},
                lines: {lineWidth: 1, fill: true, fillColor: '#f8fbfd'},
                legend: {backgroundColor: null},
                series: {shadowSize: 0, threshold: {below: 0, color: '#960505'}},
                xaxis: {ticks: 6, mode: 'time', tickLength: 0},
                yaxis: {position: 'left', color: '#4a6d9e'}
            };
            top_plot_options = $.extend(true, {}, common_plot_options, {
                colors: colors_arr,
                crosshair: {mode: 'x', color: '#e5e5e5', lineWidth: '1'},
                grid: {labelMargin: 4, hoverable: true, aboveData: true},
                lines: {fillColor: '#f8fbfd'},
                legend: {show: true, labelBoxBorderColor: 'transparent', position: 'nw', margin: 1},
                pan: {interactive: true, cursor: "move", frameRate: 30},
                selection: {mode: null},
                xaxis: {labelHeight: 14, color: '#4a6d9e', tickColor: '#fff', min: initial_preset, max: x_max},
                yaxis: {ticks: 5, panRange: false, tickLength: 'full', tickColor: '#f3f3f3', labelWidth: 40}
            });
            bot_plot_options = $.extend(true, {}, common_plot_options, {
                colors: colors_arr_p2,
                grid: {aboveData: true, labelMargin: -13, minBorderMargin: 1},
                lines: {fill: false},
                legend: {show: false},
                selection: {mode: 'x', color: '#fff'},
                xaxis: {labelHeight: 13, tickColor: '#fff', color: '#fff'},
                yaxis: {show: false}
            });
            handler = function (result) {
                if (result.error) return;
                var data = result.data,
                    show_datapoints_link = 'datapoints_link' in config ? config.datapoints_link : true,
                    init_data_field = data.template_data.data_field,
                    init_ob_time = data.template_data.observation_time,
                    data_arr = [{
                        data: data.timeseries.data,
                        label: data.template_data.observation_time,
                        data_provider: data.template_data.data_provider,
                        data_source: data.template_data.data_source,
                        object_id: data.template_data.object_id
                    }],
                    state = {}, // keeps a record of the current data sets in the plot along with zoom and pan data
                    $p1, p1_options, p1_selector = selector + ' .og-js-p1',
                    $p2, p2_options, p2_selector = selector + ' .og-js-p2',
                    tenor = selector + ' .og-tenor',
                    plot_selector = selector + ' .og-plot-header',
                    $legend, panning, hover_pos = null,
                    reset_options,
                    build_menu, empty_plots, update_legend, rescale_yaxis, resize,
                    calculate_y_values, load_data_points, get_legend;
                $(selector).html((Handlebars.compile(plot_template))({alive: alive})).css({position: 'relative'});
                $(plot_selector)
                    .html('<span class="og-checking-related">checking for related timeseries data...</span>');
                get_legend = function () {return $(selector + ' .legend');}; // the legend is often regenerated
                reset_options = function () {p1_options = top_plot_options, p2_options = bot_plot_options;};
                empty_plots = function () {
                    var d = ['1', '2'], disabled_options,
                        msg = $('<div>No data available</div>').css({
                            position: 'absolute', color: '#999', left: '10px', top: '10px'
                        });
                    reset_options();
                    load_data_points();
                    disabled_options = {
                        grid: {borderWidth: 0},
                        xaxis: {show: false},
                        yaxis: {show: false},
                        selection: {mode: null},
                        pan: {interactive: false}
                    };
                    $(tenor + ', .og-flot-xaxis').css({visibility: 'hidden'});
                    $p1 = $.plot($(p1_selector), d, $.extend(true, {}, p1_options, disabled_options));
                    $p2 = $.plot($(p2_selector), d, $.extend(true, {}, p2_options, disabled_options));
                    $(p1_selector).append(msg);
                };
                load_data_points = function () {
                    var $template, col_width, render_grid, slick_width,
                        $data_points = $(selector + ' .og-data-points');
                    if (!$data_points.length) return;
                    if (!config.datapoints) {
                        if (show_datapoints_link) $data_points.css({
                            'position': 'relative', 'top': '-30px', 'left': '3px'
                            }).html('<a href="#/timeseries/'+ config.id +'">View Timeseries with DataPoints</a>');
                        return;
                    }
                    $(selector + ' .og-data-points').html('<div class="og-container"></div>');
                    (function () { // Calculate Slickgrid "slick_width" and "col_width"
                        var num_cols = data_arr.length * 2, slick_buffer = 40, default_col = 180,
                            min_width = num_cols * ((slick_buffer / 2) + default_col),
                            container_width = $(selector + ' .og-container').width();
                        if (min_width < container_width) col_width = (container_width / num_cols) - slick_buffer / 2;
                        else col_width = default_col;
                        slick_width = (2 * col_width) + slick_buffer - 2;
                    }());
                    $template = $(selector + ' .og-data-points .og-container');
                    if (!data_arr) return $template.html('<span class="og-no-datapoint">No data available</span>');
                    render_grid = function (index) {
                        var SLICK_SELECTOR = selector + ' .og-data-points .og-slick-' + index, slick, data,
                        columns = [
                            {id: 'time', name: 'Time', field: 'time', width: col_width,
                                formatter: function (row, cell, value) {return og.common.util.date(value, 'dateonly');}
                            },
                            {id: 'value', name: 'Value', field: 'value', width: col_width}
                        ],
                        options = {
                            editable: false,
                            enableAddRow: false,
                            enableCellNavigation: false,
                            showHeaderRow: false,
                            headerRowHeight: 0
                        };
                        // clone (slice) so you don't touch original data structure, reverse order for display
                        data = data_arr[index].data.slice().reverse().reduce(function (acc, val) {
                            return acc.push({time: val[0], value: val[1]}) && acc;
                        }, []);
                        columns = og.common.slickgrid.calibrate_columns({
                            container: SLICK_SELECTOR,
                            columns: columns,
                            buffer: 17
                        });
                        try {slick = new Slick.Grid(SLICK_SELECTOR, data, columns, options);}
                        catch(e) {$(SLICK_SELECTOR + ' .og-loading').html('' + e);}
                        finally {$(SLICK_SELECTOR);}
                    };
                    data_arr.forEach(function (v, i) {
                        var $compiled = $((Handlebars.compile(data_template))({
                            time: (Object.keys(meta).length ? v.label : init_ob_time).toLowerCase().replace(/_/g, ' '),
                            index: i, color: colors_arr[i], source: data_arr[i].data_source,
                            provider: data_arr[i].data_provider
                        }));
                        $compiled.find('.og-js-timeseries-csv').bind('click', function () {
                            window.location.href = '/jax/timeseries/' + data_arr[i].object_id + '.csv';
                        }).end().appendTo($template);
                        setTimeout(render_grid.partial(i), 0);
                    });
                    $data_points.find('.og-data-series').css('width', slick_width + 'px');
                };
                load_plots = function () {
                    if (data_arr[0] === void 0 || data_arr[0].data.length < 2) {empty_plots(); return}
                    var d = data_arr, data = data_arr[0].data;
                    (function () { // set up presets
                        var new_max_date_obj, _1m, _3m, _6m, _1y, _2y, _3y, counter = 0, presets = {};
                        x_max = data[data.length-1][0];
                        new_max_date_obj = function () {return new Date(x_max);};
                        presets._1d = x_max - 86400 * 1000; // in milliseconds
                        presets._1w = x_max - 7 * 86400 * 1000;
                        _1m = new_max_date_obj(), _1m.setUTCMonth(_1m.getUTCMonth() - 1), presets._1m = +_1m;
                        _3m = new_max_date_obj(), _3m.setUTCMonth(_3m.getUTCMonth() - 3), presets._3m = +_3m;
                        _6m = new_max_date_obj(), _6m.setUTCMonth(_6m.getUTCMonth() - 6), presets._6m = +_6m;
                        _1y = new_max_date_obj(), _1y.setUTCFullYear(_1y.getUTCFullYear() - 1), presets._1y = +_1y;
                        _2y = new_max_date_obj(), _2y.setUTCFullYear(_2y.getUTCFullYear() - 2), presets._2y = +_2y;
                        _3y = new_max_date_obj(), _3y.setUTCFullYear(_3y.getUTCFullYear() - 3), presets._3y = +_3y;
                        presets._all = data[0][0];
                        $(tenor).html(['all', '3y', '2y', '1y', '6m', '3m', '1m', '1w', '1d']
                            .reduce(function (pre, cur) {
                                var is_valid = presets['_' + cur] >= presets._all,
                                    classes = is_valid ? 'OG-link og-js-' + cur : 'OG-link-disabled';
                                if (is_valid) ++counter;
                                if (!state.zoom && counter === 3) { // pick the 3rd largest valid preset by default
                                    classes += ' OG-link-active', initial_preset = presets['_' + cur];
                                    state.zoom = '_' + cur;
                                } else if (state.zoom === '_' + cur) { // or the one last set by the user
                                    classes += ' OG-link-active', initial_preset = presets[state.zoom];
                                }
                                pre.unshift('<span class="'+ classes +'" style="margin-right: 5px;">'
                                    + cur +'</span>');
                                return pre;
                            }, []).join('')).unbind().bind('click', function (e) {
                                e.stopPropagation();
                                var target = e.target, from = presets['_' + target.textContent], $elm = $(target);
                                if ($elm.hasClass('OG-link-disabled') || !$elm.is('span')) return;
                                state.zoom = '_' + target.textContent;
                                $elm.siblings().removeClass('OG-link-active'), $elm.addClass('OG-link-active');
                                state.from = from, state.to = x_max;
                                $p2.setSelection({xaxis: {from: from , to: x_max}}, true);
                                p1_options.xaxis.min = from, p1_options.xaxis.max = x_max;
                                rescale_yaxis();
                                $legend = get_legend();
                                $legend.css({visibility: 'hidden'});
                            });
                    }());
                    reset_options();
                    p1_options.xaxis.panRange = [data[0][0], data[data.length-1][0]];
                    if (!(state.from && state.to)) {state.from = initial_preset, state.to = x_max;}
                    // in xaxis, min/max sets the pan, from/to sets the selection
                    p1_options.xaxis.min = state.from, p1_options.xaxis.max = state.to;
                    p2_options.xaxis.from = state.from, p1_options.xaxis.to = state.to;
                    calculate_y_values();
                    if (data_arr.length > 1) p1_options.lines.fill = p2_options.lines.fill = false;
                    else p1_options.lines.fill = true;
                    $p1 = $.plot($(p1_selector), d, p1_options);
                    $p2 = $.plot($(p2_selector), d, p2_options);
                    $p2.setSelection({xaxis: {from: state.from, to: state.to}}, true);
                    // connect the two plots
                    $(p1_selector).unbind('plotselected').bind('plotselected', function (e, r) { // events, ranges
                        state.from = r.xaxis.from, state.to = r.xaxis.to;
                        p1_options.xaxis.min = state.from, p1_options.xaxis.max = state.to;
                        $p1 = $.plot($(p1_selector), d, p1_options);
                        $legend = get_legend(), $legend.css({visibility: 'hidden'});
                    });
                    $(p1_selector).unbind('plotpan').bind('plotpan', function (e, obj) { // panning
                        var mouseup = function () {
                            setTimeout(function () {panning = false;}, 0);
                            $(document).unbind('mouseup', mouseup);
                            rescale_yaxis();
                        },
                        xaxes = obj.getXAxes()[0];
                        panning = true;
                        $(document).bind('mouseup', mouseup);
                        $legend = get_legend(), $legend.css({visibility: 'hidden'});
                        $p2.setSelection({xaxis: {from: state.from = xaxes.min, to: state.to = xaxes.max}}, true);
                    });
                    $(p2_selector).unbind('plotselecting').bind('plotselecting', function (e, r) {
                        if (r) $p1.setSelection(r);
                        rescale_yaxis();
                    });
                    $(p2_selector).unbind('plotselected').bind('plotselected', function (e, r) {
                        $(tenor + ' .OG-link').removeClass('OG-link-active');
                    });
                    $(p1_selector).unbind('plothover').bind('plothover', function (e, pos) {
                        if (!panning) hover_pos = pos, setTimeout(update_legend, 50);
                    });
                    $legend = get_legend(), $legend.css({visibility: 'hidden'});
                    rescale_yaxis();
                    load_data_points();
                    setTimeout(function () {
                        $(selector + ' .og-plots').css('visibility', 'visible');
                        $(selector + ' .og-loading').remove();
                    }); // load smoother
                };
                calculate_y_values = function () {
                    var cur, // the current data set
                        idx_from, idx_to,// indexes used to slice [cur] to the visible range
                        sliced, // temporary sliced array
                        get_values, // function that takes an array and return the min an max values with a buffer
                        p1_vals, p2_vals,
                        i = data_arr.length,
                        arr_full = [], // the full data set (for p2)
                        arr_sel = []; // the selection data set (for p1)
                    // create 2 arrays, 1 of the VIEWABLE data points of all visible data sets, for p1,
                    // and the other of the FULL data points of all visible data sets for p2
                    while(i--) {
                        if (!data_arr[i].data.length) continue; // account for threshold data
                        cur = data_arr[i].data;
                        arr_full = arr_full.concat(cur);
                        // Find closest min / max index in x range, lazy
                        // This only needs to be done for one series
                        if (!idx_from) data_arr[0].data.map(function (v, i) {
                            if (!idx_from && v[0] >= state.from) i < 2 ? idx_from = 0 : idx_from = i - 1;
                            if (!idx_to && v[0] >= state.to) i > cur.length - 3 ? idx_to = cur.length - 1 : idx_to = i;
                        });
                        // if the number of data points between the indices is less than 10, add more
                        // this doesn't change the viewable data, just the yaxis range
                        while (idx_to - idx_from < Math.min(10, cur.length - 1)) {
                            if (idx_to !== cur.length - 1) ++idx_to;
                            if (idx_from !== 0) --idx_from;
                        }
                        sliced = data_arr[i].data.slice(idx_from, idx_to);
                        arr_sel = arr_sel.concat(sliced);
                    }
                    get_values = function (arr) {
                        var min, max, buffer;
                        max = (function (arr) {return Math.max.apply(null, arr.pluck(1));})(arr);
                        min = (function (arr) {return Math.min.apply(null, arr.pluck(1));})(arr);
                        buffer = (max - min) / 10, max += buffer, min -= buffer;
                        return {min: min, max: max}
                    };
                    p1_vals = get_values(arr_sel), p2_vals = get_values(arr_full);
                    p1_options.yaxis.min = p1_vals.min, p1_options.yaxis.max = p1_vals.max;
                    p2_options.yaxis.min = p2_vals.min, p2_options.yaxis.max = p2_vals.max;
                };
                rescale_yaxis = function () {
                    calculate_y_values();
                    p1_options.xaxis.min = state.from, p1_options.xaxis.max = state.to;
                    $p1.setSelection({
                        xaxis: {from: state.from, to: state.to},
                        yaxis: {from: p1_options.yaxis.min, to: p1_options.yaxis.max}
                    });
                };
                update_legend = function () {
                    var $legends = $(selector + ' .legendLabel'),
                        axes = $p1.getAxes(), j, dataset = $p1.getData(), i = dataset.length, series, y, p1, p2,
                        $date_elm = $legend.find('.og-date'), date, format_date, legend_height,
                        rel_cursor_pos = hover_pos.pageX - ($(selector).offset().left) + 5;
                    format_date = function (date) {
                        return new Date(date).toUTCString().replace(/(^.*:[0-9]{2}\s).*$/, '$1');
                    };
                    $legend = get_legend();
                    if (hover_pos.x < axes.xaxis.min || hover_pos.x > axes.xaxis.max ||
                        hover_pos.y < axes.yaxis.min || hover_pos.y > axes.yaxis.max) {
                        $legend.css({visibility: 'hidden'});
                        return;
                    }
                    $legends.each(function () {$(this).css('line-height', 0)});
                    $legend.find('table').css({'top': '17px'});
                    legend_height = $legend.find('table').prev().height() + 15;
                    $legend.css({
                        left: rel_cursor_pos > $(selector).width() - 310 ? rel_cursor_pos - 175 : rel_cursor_pos,
                        visibility: 'visible', 'height': legend_height + 'px'
                    });
                    $legend.find('div, table').css({'left': '5px', 'background': 'none'});
                    while (i--) {
                        if (!dataset[i].data.length) continue;
                        series = dataset[i];
                        // find the closest x points
                        for (j = 0; j < series.data.length; ++j) if (series.data[j][0] > hover_pos.x) break;
                        // now interpolate
                        p1 = series.data[j - 1], p2 = series.data[j];
                        if (p1 == null) y = p2[1], date = format_date(p2[0]); // first
                        else if (p2 == null) y = p1[1], date = format_date(p1[0]); // last
                        else y = p1[1], date = format_date(p1[0]); // all others
                        if ($date_elm.length) $date_elm.html(date);
                        else {
                            $('<div class="og-date"></div>').css({
                                position: 'absolute', top: '0', 'white-space': 'nowrap',
                                padding: '0 0 0 2px', 'font-size': '10px', 'color': '#999'
                            }).prependTo($legend);
                        }
                        if (y) $legends.eq(i).text(y.toFixed(2)); // otherwise there is no data, so no update
                    }
                };
                build_menu = function () {
                    if (config.menu === false) return $(plot_selector).empty();
                    // build meta data object and populate it with the initial plot data
                    if (result.data.related) result.data.related.forEach(function (val) {
                        var df = val.data_field;
                        if (!meta[df]) meta[df] = {};
                        meta[df][val.observation_time] = val.object_id;
                    });
                    meta[init_data_field][init_ob_time] = result;
                    state.field = init_data_field, state.time = [init_ob_time];
                    (function () {
                        // Helper function, returns a timeseries data object, or an id which can be used to get it
                        // Takes a sub object of meta
                        var get_data_or_id = function (obj) {return obj[Object.keys(obj)[0]];},
                        // build select
                        build_select = function () {
                            var field, select = '';
                            for (field in meta) select += '<option>'+ field +'</option>';
                            return select = '<div class="og-field"><select>' + select
                                + '</select></div>';
                        },
                        // build checkboxes
                        build_checkbox = function  () {
                            var time, checkbox = '';
                            for (time in meta[state.field]) {
                                checkbox += '<label><input type="checkbox" /><span>' + time + '</span></label>';
                            }
                            return checkbox = '<div class="og-observation">' + checkbox + '</div>';
                        },
                        // build form
                        build_form = function () {
                            var $form = $(build_select() + build_checkbox()), ctr = 0;
                            // Set selected options
                            $form.find('select').val(state.field);
                            $.each(state.time, function (i, time) {
                                $form.find('label span').contents().each(function (i, node) {
                                    if (time === $(node).text())
                                        $(this).parent().prev().prop('checked', 'checked').parent()
                                            .css({'color': '#fff', 'background-color': colors_arr[ctr]}), ctr += 1;
                                });
                            });
                            // attach handlers
                            $form.find('select, input').change(function (e) {
                                var meta_sub_obj, data = [], is_select = $(e.target).is('select'), new_time, index_of,
                                    handler = function (r) {
                                        var td = r.data.template_data, field = td.data_field,
                                            time = td.observation_time, t, cached_object;
                                        state.field = field, meta[field][time] = r;
                                        if (is_select) state.time = [time],
                                            data.push({
                                                data: r.data.timeseries.data,
                                                label: time,
                                                data_provider: r.data.template_data.data_provider,
                                                data_source: r.data.template_data.data_source,
                                                object_id: r.data.template_data.object_id
                                            });
                                        else for (t in state.time) {
                                            if (!meta[state.field][state.time[t]]) continue;
                                            cached_object = meta[state.field][state.time[t]].data;
                                            data.push({
                                                data: cached_object.timeseries.data,
                                                label: state.time[t],
                                                data_provider: cached_object.template_data.data_provider,
                                                data_source: cached_object.template_data.data_source,
                                                object_id: cached_object.template_data.object_id
                                            });
                                        }
                                        data_arr = data;
                                        load_plots();
                                        $(plot_selector).html(build_form());
                                    };
                                if (is_select) {
                                    state.from = state.to = void 0;
                                    meta_sub_obj = get_data_or_id(meta[$(e.target).val()]);
                                    if (typeof meta_sub_obj === 'object') return handler(meta_sub_obj);
                                    return api.rest.timeseries.get({id: meta_sub_obj}).pipe(handler);
                                }
                                new_time = $(this).next().text();
                                index_of = state.time.indexOf(new_time);
                                meta_sub_obj = meta[state.field][new_time];
                                if (~index_of) state.time.splice(index_of, 1); else state.time.push(new_time);
                                if (typeof meta_sub_obj === 'object') return handler(meta_sub_obj);
                                api.rest.timeseries.get({id: meta_sub_obj}).pipe(handler);
                            });
                            return $form;
                        };
                        $(plot_selector).html(build_form());
                    }());
                };
                build_menu();
                if (!config.child) og.common.gadgets.manager.register(timeseries);
                timeseries.resize();
            };
            $.when(
                api.text({module: 'og.views.gadgets.timeseries.plot_tash'}),
                api.text({module: 'og.views.gadgets.timeseries.data_tash'})
            ).then(function (plot_tmpl, data_tmpl) {
                plot_template = plot_tmpl, data_template = data_tmpl;
                if (spoofed_data) return handler(spoofed_data.main);
                api.rest.timeseries.get({dependencies: ['id'], id: config.id, cache_for: 10000}).pipe(handler);
            });
        };
    }
});