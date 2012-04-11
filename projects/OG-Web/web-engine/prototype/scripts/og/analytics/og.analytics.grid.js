/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.analytics.grid',
    dependencies: ['og.api.text', 'og.analytics.Data'],
    obj: function () {
        var module = this, api_text = og.api.text, counter = 1,
            css_tmpl = api_text.partial({url: module.html_root + 'analytics/grid/og.analytics.grid_tash.css'}),
            header_tmpl = api_text.partial({module: 'og.analytics.grid.header_tash'}),
            container_tmpl = api_text.partial({module: 'og.analytics.grid.container_tash'}),
            templates = null,
            scrollbar_size = 19;
        return function (config) {
            var selector = config.selector, total_width = config.width, total_height = config.height;
            $.when(css_tmpl(), header_tmpl(), container_tmpl()).then(function (css_tmpl, header_tmpl, container_tmpl) {
                var alive, resize, render_rows, render_cols,
                    dataman = new og.analytics.Data, id = '#analytics_grid_' + counter++, col_css, $style;
                if (!templates) templates = {
                    css: Handlebars.compile(css_tmpl),
                    header: Handlebars.compile(header_tmpl),
                    container: Handlebars.compile(container_tmpl)
                };
                load_css = function (css) {
                    $style = $('<style type="text/css" />').appendTo($('head'));
                    if ($style[0].styleSheet) return $style[0].styleSheet.cssText = css; // IE
                    $style[0].appendChild(document.createTextNode(css));
                };
                col_css = function (columns, col_offset) {
                    var partial_width = 0, total_width = columns.reduce(function (acc, val) {
                        return +val.width + acc;
                    }, 0);
                    col_offset = col_offset || 0;
                    return columns.map(function (val, idx) {
                        var width = +val.width, css = {
                            prefix: id,
                            selector: 'c' + (idx + col_offset),
                            left: partial_width, right: total_width - partial_width - width
                        };
                        partial_width += width;
                        return css;
                    })
                };
                render_cols = function (columns) {
                    var width = columns.width, gen_cols = function (columns, start_at) {
                        return templates.header({
                            width: start_at ? width.scroll : width.fixed,
                            padding_right: start_at ? scrollbar_size : 0,
                            cols: columns.reduce(function (acc, val, idx) {
                                return acc + '<div class="OG-g-h-col c' + (idx + (start_at || 0)) + '">' + val.name +
                                    '</div>';
                            }, '')
                        });
                    };
                    $(id + ' .OG-g-h-fixed').html(gen_cols(columns.fixed));
                    $(id + ' .OG-g-h-scroll').html(gen_cols(columns.scroll, columns.fixed.length));
                };
                render_rows = function (columns, data) {
                    var gen_rows = function (fixed) {
                        var html, col_from = fixed ? 0 : columns.fixed.length, height = data.length * 20,
                            col_to = fixed ? columns.fixed.length : columns.fixed.length + columns.scroll.length,
                            start = '<div class="OG-g-rows" style="height: ' + (height + (fixed ? scrollbar_size : 0))
                                + 'px">';
                        html = data.reduce(function (acc, row, idx) {
                            acc.push('<div class="OG-g-row r'+ idx +'" style="top: ' + (idx * 20) + 'px">');
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
                initialize = function (columns) {
                    var header = {height: '51'}; // Stub
                    columns.width = (function () { // width of fixed and scrollable column areas
                        return {
                            fixed: columns.fixed.reduce(function (acc, val) {return acc + val.width;}, 0),
                            scroll: columns.scroll.reduce(function (acc, val) {return acc + val.width;}, 0)
                        };
                    })();
                    var viewport = {width: total_width - columns.width.fixed, height: total_height}, css_data = {
                        id: id,
                        height: viewport.height,
                        viewport_width: viewport.width,
                        scroll_width: columns.width.scroll,
                        header_height: header.height,
                        fixed_width: columns.width.fixed,
                        columns: col_css(columns.fixed)
                            .concat(col_css(columns.scroll, columns.fixed.length))
                    };
                    load_css(templates.css(css_data));
                    $(selector).html(templates.container({
                        id: id.substring(1), height: viewport.height, width: total_width
                    }));
                    $(id + ' .OG-g-b-scroll').scroll(function (e) { // sync scroll
                        $(id + ' .OG-g-b-fixed').scrollTop(e.target.scrollTop);
                        $(id + ' .OG-g-h-scroll').scrollLeft(e.target.scrollLeft);
                    });
                    render_cols(columns);
                    dataman.on('data', render_rows.partial(columns));
                    og.common.gadgets.manager.register({
                        alive: function () {return !!$(id).length ? true : !$style.remove();},
                        resize: function () {console.log('grid resize');}
                    });
                };
                dataman.on('init', initialize);
            });
        };
    }
});