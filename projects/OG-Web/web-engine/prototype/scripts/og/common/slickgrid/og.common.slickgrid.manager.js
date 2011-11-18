/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.slickgrid.manager',
    dependencies: ['og.common.util.ui.message'],
    obj: function () {
        return function (obj) {
            var DEFAULT_PAGESIZE = 20,
                data = {length: 0},
                on_data_loading = new Slick.Event(),
                on_data_loaded = new Slick.Event(),
                request, timer, ensure_data;
            /**
             * Fetches the data and populates the data object required for SlickGrid
             */
            ensure_data = function (args) {
                var from = args.from,
                    to = args.to,
                    from_page = Math.floor(from / DEFAULT_PAGESIZE),
                    to_page = Math.floor(to / DEFAULT_PAGESIZE),
                    request_page_size,
                    request_page_number,
                    data_already_cached = false,
                    ui = og.common.util.ui,
                    filters = args.filters,
                    is_new_filter = args.filter_being_applied || false,
                    handle_data;
                delete args.filters.version;
                if (!is_new_filter) {
                    while (data[from_page * DEFAULT_PAGESIZE] !== undefined && from_page < to_page) from_page++;
                    while (data[to_page * DEFAULT_PAGESIZE] !== undefined && from_page < to_page) to_page--;
                } else for (var i = 0; i < data.length; i++) if (data[i]) delete data[i]; // Delete old data
                // Get page size/number
                request_page_size = (((to_page - from_page) * DEFAULT_PAGESIZE) + DEFAULT_PAGESIZE);
                request_page_number = Math.floor(from_page / (request_page_size / DEFAULT_PAGESIZE));
                (function () {
                    // The search is always different if filters is populated on keyup
                    // so only check if cached data exists if a new filter has not been applied
                    var from_num, to_num, i;
                    if (is_new_filter) {
                        from_page = 0;
                        $(obj.selector + ' .slick-viewport').scrollTop(0);
                    }
                    else {
                        from_num = request_page_number * request_page_size;
                        to_num = 'total' in data ? Math.min(from_num + request_page_size, data.total)
                            : from_num + request_page_size;
                        for (i = from_num; i < to_num; i++) data_already_cached = data[i] !== undefined || false;
                    }
                }());
                // Rest handler
                handle_data = function (r) {
                    var from, to, json_header;
                    if (r.error) {
                        ui.message({
                            location: '.OG-js-search',
                            css: {bottom: '6px'},
                            message: 'oops, something bad happened (' + r.message + ')'
                        });
                        return;
                    }
                    json_header = r.data.header;
                    from = from_page * DEFAULT_PAGESIZE;
                    to = from + json_header.pgSze;
                    data.length = parseInt(json_header.pgTtl);
                    // Create Data Object for slickgrid
                    data.total = r.data.header.pgTtl;
                    $.each(r.data.data, function (i, row) {
                        var field_values = row.split('|'),
                            field_names = json_header.dataFields,
                            tmp_val;
                        data[from + i] = {};
                        $.each(field_names, function (k, field_name) {
                            tmp_val = field_values[k];
                            if (field_names[k] === 'type') {
                                data[from + i][field_name] = tmp_val;
                            } else data[from + i][field_name] = tmp_val;
                        });
                        if (filters.type) data[from + i].type = filters.type.replace(/_/g, ' ');
                        data[from + i].index = from + i;
                    });
                    on_data_loaded.notify({from: from,to: to});
                    ui.message({location: '.OG-js-search', destroy: true});
                    clearTimeout(timer);
                };
                /**
                 * Do rest request
                 */
                if (!data_already_cached) {
                    og.api.rest.abort(request);
                    request = og.api.rest[obj.page_type].get($.extend({
                        handler: handle_data,
                        loading: function () {
                            ui.message({
                                location: '.OG-js-search',
                                css: {bottom: '6px'},
                                message: {0: 'loading...', 3000: 'still loading...'}});
                        },
                        page_size: request_page_size,
                        page: request_page_number + 1, // 0 and 1 are the same
                        search: true
                    }, (function () {
                        var fields = ['name', 'type', 'data_source', 'identifier', 'data_provider',
                            'data_field', 'ob_time', 'ob_date', 'observation_time', 'status', 'quantity'];
                        return fields.reduce(function (acc, val) {
                            if (!filters[val]) return acc;
                            if (val === 'type') return acc[val] = filters.type, acc;
                            if (val === 'quantity') {
                                acc.min_quantity = filters.min_quantity, acc.max_quantity = filters.max_quantity;
                                return acc;
                            }
                            if (val === 'ob_date')
                                if (/19|20[0-9]{2}-[01][0-9]-[0123][0-9]/.test(filters['ob_date']))
                                    return acc['observation_date'] = filters[val], acc;
                                else return acc;
                            if (val === 'ob_time') {
                                return acc['observation_time'] = ('*' + filters[val] + '*').replace(/\s/g, '*'), acc
                            }
                            return acc[val] = ('*' + filters[val] + '*').replace(/\s/g, '*'), acc;
                        }, {});
                    }())));
                }
            };
            return {
                data: data,                                  // properties
                on_data_loading: on_data_loading,            // events
                on_data_loaded: on_data_loaded,              // events
                ensure_data: ensure_data                     // methods
            };
        }
    }
});