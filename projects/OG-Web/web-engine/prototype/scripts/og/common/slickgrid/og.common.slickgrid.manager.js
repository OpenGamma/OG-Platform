/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */

$.register_module({
    name: 'og.common.slickgrid.manager',
    dependencies: ['og.common.util.ui.message'],
    obj: function () {

        return function (obj) {
            var DEAFULT_PAGESIZE = 20,
                data = {length: 0},
                on_data_loading = new Slick.Event(),
                on_data_loaded = new Slick.Event(),
                timer;
            /**
             * Fetches the data and populates the data object required for SlickGrid
             */
            function ensure_data(args) {
                var from = args.from,
                    to = args.to,
                    from_page = Math.floor(from / DEAFULT_PAGESIZE),
                    to_page = Math.floor(to / DEAFULT_PAGESIZE),
                    request_page_size,
                    request_page_number,
                    data_already_cached = false,
                    ui = og.common.util.ui,
                    filters = args.filters,
                    is_new_filter = args.filter_being_applied || false;

                if (!is_new_filter) {
                    while (data[from_page * DEAFULT_PAGESIZE] !== undefined && from_page < to_page) from_page++;
                    while (data[to_page * DEAFULT_PAGESIZE] !== undefined && from_page < to_page) to_page--;
                } else for (var i = 0; i < data.length; i++) if (data[i]) delete data[i]; // Delete old data

                // Get page size/number
                request_page_size = (((to_page - from_page) * DEAFULT_PAGESIZE) + DEAFULT_PAGESIZE);
                request_page_number = Math.floor(from_page / (request_page_size / DEAFULT_PAGESIZE));

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
                        to_num = from_num + request_page_size;
                        for (i = from_num; i < to_num; i++) data_already_cached = data[i] !== undefined || false;
                    }
                }());

                // Rest handler
                function handle_data(r) {
                    var from, to, json_header;
                    if (r.error) {
                        ui.message({
                            location: '#OG-sr',
                            message: 'oops, something bad happened (' + r.message + ')'
                        });
                        return;
                    }
                    json_header = r.data.header;
                    from = from_page * DEAFULT_PAGESIZE;
                    to = from + json_header.count;
                    data.length = parseInt(json_header.total);
                    // Create Data Object for slickgrid
                    $.each(r.data.data, function (i, row) {
                        var field_values = row.split('|'),
                            field_names = json_header.dataFields,
                            tmp_val;
                        data[from + i] = {};
                        $.each(field_names, function (k, field_name) {
                            (function () { // this is only for timeseries
                                var id_type, id_name,
                                    id = field_values[k].split('&'),
                                    q = id.length;
                                if (id) {
                                    while (q--) {
                                        id_type = id[q].split('~')[0];
                                        id_name = id[q].split('~')[1];
                                        data[from + i][id_type] = id_name;
                                    }
                                }
                            })();
                            tmp_val = field_values[k];
                            if (field_names[k] === 'type') {
                                data[from + i][field_name] = tmp_val.toLowerCase();
                            } else data[from + i][field_name] = tmp_val;
                        });
                        if (filters.filter_type) data[from + i].type = filters.filter_type.replace(/_/g, ' ');
                        data[from + i].index = from + i;
                    });
                    on_data_loaded.notify({from: from,to: to});
                    ui.message({location: '#OG-sr', destroy: true});
                    clearTimeout(timer);
                }
                /**
                 * Do rest request
                 */
                if (!data_already_cached) {
                    og.api.rest[obj.page_type].get($.extend({
                        handler: handle_data,
                        loading: function () {
                            ui.message({location: '#OG-sr', message: {0: 'loading...', 3000: 'still loading...'}});
                        },
                        page_size: request_page_size,
                        page: request_page_number
                    }, (function () {
                           var t = {};
                           if (filters.name) t.name = ('*' + filters.name + '*').replace(/\s/g, '*');
                           if (filters.filter_type) t.type = filters.filter_type.replace('option', 'equity_option');
                           if (filters.dataSource) t.data_source = ('*' + filters.dataSource + '*').replace(/\s/g, '*');
                           if (filters.quantity) {
                               t.min_quantity = filters.min_quantity;
                               t.max_quantity = filters.max_quantity;
                           }
                           return t;
                    }())));
                }
            }
            return {
                "data": data,                                  // properties
                "on_data_loading": on_data_loading,            // events
                "on_data_loaded": on_data_loaded,              // events
                "ensure_data": ensure_data                     // methods
            };
        }
    }
});