/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.search_results.core',
    dependencies: ['og.common.routes', 'og.common.slickgrid.manager'],
    obj: function () {
        return function () {
            var routes = og.common.routes, slick_manager, options, timer, grid, filters_obj = {}, update_header_rows,
                load = function (obj) {
                    slick_manager = og.common.slickgrid.manager($.extend({}, obj.url, {
                        page_type: obj.page_type, selector: obj.selector
                    }));
                    options = $.extend({}, obj.options, {
                        editable: false,
                        enableAddRow: false,
                        enableCellNavigation: false,
                        showHeaderRow: true
                    });
                    grid = new Slick.Grid(obj.selector, slick_manager.data, obj.columns, options);
                    filters_obj = obj.url;

                    // Setup filter inputs
                    update_header_rows = function () {
                        var i, id, header, filter_class, col = obj.columns, len = obj.columns.length, init_value,
                            filter_type, filter_type_options, input_html, select_html;
                        for (i = 0; i < len; i++) {
                            id = col[i].id;
                            header = grid.getHeaderRowColumn(id);
                            filter_class = 'og-js-' + id + '-filter';
                            init_value = obj.url[id];
                            filter_type = col[i].filter_type;
                            filter_type_options = col[i].filter_type_options;
                            $(header).empty();
                            if (!obj.url) return;
                            if (!init_value) init_value = '';
                            input_html = function () {
                                return '<input type="text" class="'+ filter_class +'" value="' + init_value + '">';
                            };
                            select_html = function () {
                                return '<select class="'+ filter_class +'">' +
                                    (filter_type_options || []).reduce(function (p, v, i) {
                                        if (i === 0) p += '<option value="">' + id + '</option>';
                                        return p + '<option value="' + v + '">' + v.replace(/_/g, ' ') + '</option>';
                                    }, '') + '</select>'
                            };
                            $(({input: input_html(), select: select_html()})[filter_type])
                                    .data('columnId', id).width($(header).width() - 10).appendTo(header);
                            // update the select to match the filter, bookmark support
                            if (filter_type === 'select' && routes.current().args.filter_type)
                                    $('.' + filter_class).val(routes.current().args.filter_type.toLowerCase());
                        }
                        og.common.search.filter({location: obj.selector});
                    };
                    update_header_rows();

                    // Handle click
                    $(obj.selector).undelegate().delegate('[row]', 'click', function (e) {
                        var last = routes.last(), obj_url = obj.url,
                            params = {
                                id: slick_manager.data[$(e.currentTarget).attr('row')].id,
                                type: slick_manager.data[$(e.currentTarget).attr('row')].type,
                                name: (last && last.args.name) || '',
                                quantity: (last && last.args.quantity) || '',
                                filter_type: (last && last.args.filter_type) || '',
                                filter: slick_manager.data[$(e.currentTarget).attr('row')].filter
                            };
                        delete obj_url.node;
                        routes.go(routes.hash(
                            og.views[obj.page_type].rules['load_' + obj.page_type], $.extend({}, obj.url, params)));
                    });

                    grid.onViewportChanged.subscribe(function () {
                        clearTimeout(timer);
                        timer = setTimeout(function () {filter($.extend(true, filters_obj, {filter: false}))}, 150);
                    });

                    grid.onColumnsReordered.subscribe(function() {
                        update_header_rows();
                    });

                    grid.onColumnsResized.subscribe(function() {
                        update_header_rows();
                    });

                    // Prepare grid for new data
                    slick_manager.on_data_loaded.subscribe(function (e, args) {
                        for (var i = args.from; i <= args.to; i++) grid.invalidateRow(i);
                        grid.updateRowCount();
                        grid.render();
                    });

                    $(grid.getHeaderRow()).delegate(":input", "change keyup", function(e) {
                        obj.url[$(this).data("columnId")] = e.currentTarget.value;
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