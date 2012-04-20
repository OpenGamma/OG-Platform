/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.analytics.grid',
    dependencies: ['og.api.text', 'og.analytics.Data'],
    obj: function () {
        var module = this, api_text = og.api.text, counter = 1, compile = Handlebars.compile,
            css_tmpl = api_text.partial({url: module.html_root + 'analytics/grid/og.analytics.grid_tash.css'}),
            header_tmpl = api_text.partial({module: 'og.analytics.grid.header_tash'}),
            container_tmpl = api_text.partial({module: 'og.analytics.grid.container_tash'}),
            templates = null, col_css,
            scrollbar_size = 19, header_height = 49, row_height = 19;
        col_css = function (id, columns, col_offset) {
            var partial_width = 0, total_width = columns.reduce(function (acc, val) {
                return +val.width + acc;
            }, 0);
            return columns.map(function (val, idx) {
                var width = +val.width, css = {
                    prefix: id, selector: 'c' + (idx + (col_offset || 0)),
                    left: partial_width, right: total_width - partial_width - width
                };
                return (partial_width += width), css;
            });
        };
        return function (config) {
            var selector = config.selector, id = '#analytics_grid_' + counter++,
                total_width = config.width, total_height = config.height,
                viewport_height, visible_rows,
                scroll_observer, viewport = {}, $style, load_css, meta, dataman, busy = false;
            load_css = function (css) {
                $style = $('<style type="text/css" />').appendTo($('head'));
                if ($style[0].styleSheet) return $style[0].styleSheet.cssText = css; // IE
                $style[0].appendChild(document.createTextNode(css));
            };
            scroll_observer = (function (timeout) {
                var $fixed, $head, set_viewport, top = 'scrollTop', left = 'scrollLeft';// cache elements here
                set_viewport = function () {
                    var top_position = $fixed[top](), row_start;
                    viewport.rows = [
                        (row_start = Math.floor((top_position / viewport_height) * meta.rows)),
                        row_start + visible_rows
                    ];
                    dataman.viewport(viewport);
                    busy = false;
                };
                return function (event) { // sync scroll
                    busy = true;
                    ($fixed || ($fixed = $(id + ' .OG-g-b-fixed')))[top](this[top]);
                    ($head || ($head = $(id + ' .OG-g-h-scroll')))[left](this[left]);
                    timeout = clearTimeout(timeout) || setTimeout(set_viewport, 200);
                }
            })(null);
            $.when(css_tmpl(), header_tmpl(), container_tmpl()).then(function (css_tmpl, header_tmpl, container_tmpl) {
                var alive, resize, render_rows, render_cols;
                dataman = new og.analytics.Data;
                if (!templates) templates = {
                    css: compile(css_tmpl), header: compile(header_tmpl), container: compile(container_tmpl)
                };
                render_cols = function () {
                    var width = meta.columns.width, gen_cols = function (column_set, start_at) {
                        return templates.header({
                            width: start_at ? width.scroll : width.fixed,
                            padding_right: start_at ? scrollbar_size : 0,
                            cols: column_set.reduce(function (acc, val, idx) {
                                return acc + '<div class="OG-g-h-col c' + (idx + (start_at || 0)) + '">' + val.name +
                                    '</div>';
                            }, '')
                        });
                    };
                    $(id + ' .OG-g-h-fixed').html(gen_cols(meta.columns.fixed));
                    $(id + ' .OG-g-h-scroll').html(gen_cols(meta.columns.scroll, meta.columns.fixed.length));
                };
                render_rows = function (data) {
                    if (busy) return; // just bail
                    var gen_rows = function (fixed) {
                        var html, columns = meta.columns, col_from = fixed ? 0 : columns.fixed.length,
                            col_to = fixed ? columns.fixed.length : columns.fixed.length + columns.scroll.length,
                            start = '<div class="OG-g-rows" style="height: ' +
                                (viewport_height + (fixed ? scrollbar_size : 0)) + 'px">',
                            top_offset = Math.floor(viewport.rows[0] / meta.rows) * row_height;
                        html = data.reduce(function (acc, row, idx) {
                            var top = (idx + viewport.rows[0]) * row_height;
                            acc.push('<div class="OG-g-row r'+ idx +'" style="top: ' + top + 'px">');
                            acc.push.apply(acc, row.slice(col_from, col_to).reduce(function (acc, val, idx) {
                                return acc.concat('<div class="OG-g-cell c' + (col_from + idx) + '">' + val + '</div>');
                            }, []));
                            acc.push('</div>\n\n');
                            return acc;
                        }, [start]);
                        html.push('</div>');
                        return html.join('');
                    };
                    $(id + ' .OG-g-b-fixed').html(gen_rows(true));
                    $(id + ' .OG-g-b-scroll').html(gen_rows(false));
                };
                initialize = function (metadata) {
                    var columns = metadata.columns;
                    meta = metadata; // set instance-wide reference
                    viewport_height = meta.rows * row_height;
                    visible_rows = Math.ceil((total_height - header_height) / row_height);
                    columns.width = (function () { // width of fixed and scrollable column areas
                        return {
                            fixed: columns.fixed.reduce(function (acc, val) {return acc + val.width;}, 0),
                            scroll: columns.scroll.reduce(function (acc, val) {return acc + val.width;}, 0)
                        };
                    })();
                    load_css(templates.css({
                        id: id,
                        height: total_height - header_height,
                        header_height: header_height,
                        row_height: row_height,
                        viewport_width: total_width - columns.width.fixed,
                        scroll_width: columns.width.scroll,
                        fixed_width: columns.width.fixed,
                        columns: col_css(id, columns.fixed)
                            .concat(col_css(id, columns.scroll, columns.fixed.length))
                    }));
                    $(selector)
                        .html(templates.container({id: id.substring(1), height: total_height, width: total_width}));
                    $(id + ' .OG-g-b-scroll').scroll(scroll_observer);
                    render_cols();
                    dataman.viewport(viewport = {rows: [0, Math.min(meta.rows, visible_rows)]});
                    dataman.on('data', render_rows);
                    og.common.gadgets.manager.register({
                        alive: function () {return $(id).length ? true : !$style.remove();},
                        resize: function () {console.log('grid resize');}
                    });
                };
                dataman.on('init', initialize);
            });
        };
    }
});