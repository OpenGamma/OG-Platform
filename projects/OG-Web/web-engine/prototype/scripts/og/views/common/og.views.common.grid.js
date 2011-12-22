/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.grid',
    dependencies: ['og.common.util.ui.message'],
    obj: function () {
        var ui = og.common.util.ui;
        return function (config) {
            if (typeof config !== 'object') throw new TypeError('og.common.grid: config must be an object');
            var module = this, i, timer, slick, data = {length: 0},
                from_page = 1,
                pagesize = config.rest_options.page_size || 200,
                SELECTOR = config.selector,
                RESOURCE = config.resource,
                dependencies = config.dependencies,
                rest_options = config.rest_options,
                post_handler = config.handlers.post,
                data_handler = config.handlers.data,
                evt_handlers = config.handlers.event,
                on_data_loaded = new Slick.Event(),
                filters = config.filters,
                columns = config.columns,
                deps_counter = 0,
                slick_options = $.extend({
                    editable: false,
                    enableAddRow: false,
                    enableCellNavigation: false,
                    showHeaderRow: false,
                    headerHeight: 33
                }, config.slick_options);
            if (filters.name) filters.name = ('*' + filters.name + '*').replace(/\s/g, '*'); // process name filter
            rest_options = $.extend({}, {
                handler: function (r) {
                    if (r.error) return;
                    var evt_type, new_data = data_handler(r.data.data);
                    data.length = r.data.header.total;
                    // populate the data object
                    $.each(new_data, function (i, row) {data[((pagesize * from_page) - pagesize) + i] = row;});
                    // subscribe event handers
                    for (evt_type in evt_handlers) slick[evt_type].subscribe(evt_handlers[evt_type](r.data.data));
                    post_handler({location: SELECTOR});
                    // TODO: notify grid
                    on_data_loaded.notify();
                    ui.message({location: SELECTOR, destroy: true});
                },
                loading: function () {
                    ui.message({location: '.OG-js-search', message: {0: 'loading...', 3000: 'still loading...'}});
                }
            }, rest_options, filters);
            // fire all dependency functions, when everything has returned, load the grid
            i = dependencies.length;
            while(i--) {
                dependencies[i](config, function (new_config) {
                    deps_counter += 1, module.config = new_config;
                    if (deps_counter === dependencies.length) load();
                });
            }
            // dependencies have loaded
            var load = function () {
                // create new grid
                slick = new Slick.Grid(SELECTOR, data, columns, slick_options);
                on_data_loaded.subscribe(function (e, args) { // update the grid
                    slick.updateRowCount();
                    slick.render();
                });
                slick.onViewportChanged.subscribe(function () {
                    clearTimeout(timer);
                    timer = setTimeout(function () {
                        og.api.rest[RESOURCE].get($.extend({}, rest_options, {page_size: 50, page: 2}));
                        var vp = slick.getViewport();
                    }, 150);
                });
                og.api.rest[RESOURCE].get($.extend({}, rest_options, {page_size: 50, page: 1}));
            };

        }
    }
});