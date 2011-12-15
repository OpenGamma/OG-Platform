/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.search_results.core',
    dependencies: ['og.common.routes', 'og.common.slickgrid.manager'],
    obj: function () {
        return function () {
            var routes = og.common.routes, slick_manager, options, timer, grid, filters_obj = {},
                load = function (obj) {
                    slick_manager = og.common.slickgrid.manager(
                        $.extend({}, obj.url, {page_type: obj.page_type, selector: obj.selector})
                    );
                    options = $.extend({}, obj.options, {
                        editable: false,
                        enableAddRow: false,
                        enableCellNavigation: false,
                        showHeaderRow: false,
                        headerHeight: 38
                    });
                    obj.columns = og.common.slickgrid.calibrate_columns({
                        container: '.OG-js-search',
                        columns: obj.columns,
                        buffer: 17
                    });
                    grid = new Slick.Grid(obj.selector, slick_manager.data, obj.columns, options);
                    window.onresize = function () {
                        setTimeout(function () {
                            grid.resizeCanvas();
                            filter($.extend(true, filters_obj, {filter: false}));
                        }, 300);
                    };
                    filters_obj = obj.url;
                    // Setup filter inputs
                    og.common.search.filter({location: obj.selector});
                    // Handle click
                    $(obj.selector).undelegate().delegate('[row]', 'click', function (e) {
                        var last = routes.last(), obj_url = obj.url,
                            params = {
                                id: slick_manager.data[$(e.currentTarget).attr('row')].id,
                                name: (last && last.args.name) || '',
                                quantity: (last && last.args.quantity) || '',
                                type: (last && last.args.type) || '',
                                filter: slick_manager.data[$(e.currentTarget).attr('row')].filter,
                                version: '',
                                sync: ''
                            };
                        delete obj_url.node;
                        routes.go(routes.hash(
                            og.views[obj.page_type].rules['load_' + obj.page_type], $.extend({}, obj.url, params)));
                    });

                    grid.onViewportChanged.subscribe(function () {
                        clearTimeout(timer);
                        timer = setTimeout(function () {filter($.extend({}, filters_obj, {filter: false}))}, 150);
                    });

                    // Prepare grid for new data
                    slick_manager.on_data_loaded.subscribe(function (e, args) {
                        for (var i = args.from; i <= args.to; i++) grid.invalidateRow(i);
                        grid.updateRowCount();
                        grid.render();
                    });

                    $(grid.getHeaderRow()).delegate(':input', 'change keyup', function(e) {
                        obj.url[$(this).data('columnId')] = e.currentTarget.value;
                    });

                    // load the first page
                    grid.onViewportChanged.notify();

                },
                filter = function (obj) {
                    var vp = grid.getViewport();
                    filters_obj = obj;
                    slick_manager.ensure_data({
                        from: vp.top, to: vp.bottom, filters: filters_obj, filter_being_applied: obj.filter
                    });
                };
            return {load: load, filter: filter}
        }
    }
});