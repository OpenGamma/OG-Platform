/**
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.search_results.core',
    dependencies: ['og.common.routes', 'og.common.slickgrid.manager'],
    obj: function () {
        return function () {
            var routes = og.common.routes, slick_manager, options, timer, grid,
                process_args = function () {
                    var args = routes.current().args;
                    if (args.quantity) $.extend(args, og.common.search.get_quantities(args.quantity));
                    return args;
                },
                load = function (obj) {
                    var current_args = process_args();
                    slick_manager = og.common.slickgrid.manager(
                        $.extend({}, process_args(), {page_type: obj.page_type, selector: obj.selector})
                    );
                    options = $.extend({}, obj.options, {
                        editable: false,
                        enableAddRow: false,
                        enableCellNavigation: false,
                        showHeaderRow: false,
                        headerHeight: 29,
                        headerCssClass: '.slick-header-search',
                        enableColumnReorder: false
                    });
                    obj.columns = og.common.slickgrid.calibrate_columns({
                        container: '.OG-js-search',
                        columns: obj.columns,
                        buffer: 17
                    });
                    grid = new Slick.Grid(obj.selector, slick_manager.data, obj.columns, options);
                    $(obj.selector).click(function () {
                        $(document).trigger('mousedown.blurkill');
                    });
                    grid.setSelectionModel(new Slick.RowSelectionModel);
                    $(window).on('resize', function () {
                        setTimeout(function () {
                            var args = process_args();
                            args.filter = false;
                            grid.resizeCanvas();
                            filter(args);
                        }, 300);
                    });
                    // Setup filter inputs
                    og.common.search.filter({location: obj.selector});
                    grid.onClick.subscribe(function (e, dd) {
                        var current = routes.current().args;
                        routes.go(routes.hash(og.views[obj.page_type].rules.load_item, current, {
                            del: og.views[obj.page_type].extra_params,
                            add: {
                                id: slick_manager.data[dd.row].id,
                                name: current.name || '',
                                quantity: current.quantity || '',
                                type: current.type || '',
                                ob_date: current.ob_date || '',
                                ob_time: current.ob_time || '',
                                filter: slick_manager.data[dd.row].filter
                            }
                        }));
                    });

                    grid.onViewportChanged.subscribe(function () {
                        clearTimeout(timer);
                        timer = setTimeout(function () {filter($.extend(process_args(), {filter: false}));}, 150);
                    });

                    // Prepare grid for new data
                    slick_manager.on_data_loaded.subscribe(function (e, args) {
                        for (var i = args.from; i <= args.to; i++) grid.invalidateRow(i);
                        grid.updateRowCount();
                        grid.render();
                    });

                    // load the first page
                    grid.onViewportChanged.notify();

                },
                filter = function (filters_obj) {
                    var vp = grid.getViewport(), filters_obj = filters_obj || process_args();
                    slick_manager.ensure_data({
                        from: vp.top, to: vp.bottom, filters: filters_obj, filter_being_applied: filters_obj.filter
                    });
                };
            return {load: load, filter: filter}
        }
    }
});