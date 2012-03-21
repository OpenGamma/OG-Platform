/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.analytics.grid',
    dependencies: ['og.api.text', 'og.analytics.Data'],
    obj: function () {
        var api_text = og.api.text, counter = 1,
            header_tmpl = {module: 'og.analytics.grid.header'},
            container_tmpl = {module: 'og.analytics.grid.container'},
            scrollbar_size = 19;
        return function (config) {
            var selector = config.selector;
            $.when(api_text(header_tmpl), api_text(container_tmpl)).then(function (header_tmpl, container_tmpl) {
                var gen_rows, gen_head, gen_data, gen_css, viewport, header, width, columns,
                    data = new og.analytics.Data, id = 'analytics_grid_' + counter++;
                $(selector).html($.tmpl(container_tmpl, {id: id}));
                id = '#' + id;
                gen_data = function (cols) {
                    var matrix = [], i = 60, k, gen_num = function () {
                        return (Math.random() * 1000);
                    };
                    while (i--) {
                      k = 17;
                      matrix.push(function () {
                          var row = [];
                          while(k--) row.push(gen_num());
                          return row;
                      }());
                    }
                    return matrix;
                };
                gen_rows = function (config) {
                    var fixed = config.fixed,
                        data = gen_data(17), html, len = data.length * 20;
                    html = data.reduce(function (acc, row, idx) {
                        var value = data[idx], lcv = fixed ? 0 : columns.columns_fixed.length,
                            len = fixed ? columns.columns_fixed.length : value.length;
                        acc.push('<div class="OG-g-row r'+ idx +'" style="top: ' + (idx * 20) + 'px">');
                        for (; lcv < len; lcv += 1)
                            acc.push('<div class="OG-g-cell c' + lcv + '">' + value[lcv] + '</div>');
                        acc.push('</div>\n\n');
                        return acc;
                    }, ['<div class="OG-g-rows" style="height: ' + (len + scrollbar_size) + 'px">']);
                    html.push('</div>');
                    return html.join('');
                };
                gen_head = function (columns, start_at) {
                    return $.tmpl(header_tmpl, {
                        width: start_at ? width.scroll : width.fixed,
                        padding_right: start_at ? scrollbar_size : 0,
                        cols: columns.reduce(function (acc, val, idx) {
                            return acc + '<div class="OG-g-h-col c' + (idx + (start_at || 0)) + '">' +
                                val.name + '</div>';
                        }, '')
                    });
                };
                gen_css = function () {
                    var $style = $('<style type="text/css" />').appendTo($('head')), css, gen_group;
                    gen_group = function (cols, start_at) {
                        var pwidth = 0, all_cols_width = cols.reduce(function (acc, val, i) {
                            return acc + parseInt(val.width);
                        }, 0);
                        return cols.reduce(function (acc, val, i) {
                            var width = val.width;
                            acc.push(id + ' .OG-g .c' + (i + (start_at || 0)) +
                                '{left: ' + pwidth + 'px; right: ' + (all_cols_width - pwidth - width) + 'px;}');
                            pwidth += width;
                            return acc;
                        }, []).join('\n');
                    };
                    css = [
                        id + ' .OG-g-b-scroll {width: ' + viewport.width + 'px; left: ' + width.fixed + 'px; height: '+
                            viewport.height +'px}',
                        id + ' .OG-g-h-scroll {width: ' + viewport.width + 'px; height: ' + header.height +
                            'px; left: ' + width.fixed + 'px}',
                        id + ' .OG-g-b-scroll .OG-g-row {width: ' + width.scroll + 'px}',
                        id + ' .OG-g-b-fixed {width: ' + width.fixed + 'px; height: '+ viewport.height + 'px}',
                        id + ' .OG-g-h-fixed {width: ' + width.fixed + 'px}',
                        id + ' .OG-g-b {top: ' + header.height + 'px}',
                        gen_group(columns.columns_fixed, 0),
                        gen_group(columns.columns_scroll, columns.columns_fixed.length)
                    ].join('\n');
                    if ($style[0].styleSheet) $style[0].styleSheet.cssText = css; // IE
                    else $style[0].appendChild(document.createTextNode(css));
                };
                data.init(function (result) {
                    columns = result;
                    viewport = {width: '800', height: '600'}; // Stub
                    header = {height: '51'}; // Stub
                    width = (function (columns) { // width of fixed and scrollable column areas
                        return {
                            fixed: columns.columns_fixed.reduce(function (acc, val) {return acc + val.width}, 0),
                            scroll: columns.columns_scroll.reduce(function (acc, val) {return acc + val.width}, 0)
                        };
                    })(columns);
                    gen_css();
                    $(id + ' .OG-g-b-scroll').scroll(function (e) { // sync scroll
                        $(id + ' .OG-g-b-fixed').scrollTop(e.target.scrollTop);
                        $(id + ' .OG-g-h-scroll').scrollLeft(e.target.scrollLeft);
                    });
                    $(id + ' .OG-g-b-fixed').html(gen_rows({fixed: true}));
                    $(id + ' .OG-g-b-scroll').html(gen_rows(columns.columns_scroll.length, columns.columns_fixed.length));
                    $(id + ' .OG-g-h-fixed').html(gen_head(columns.columns_fixed));
                    $(id + ' .OG-g-h-scroll').html(gen_head(columns.columns_scroll, columns.columns_fixed.length));
                    var scroll_len = columns.columns_scroll.length;
                    var fixed_num = columns.columns_fixed.length;
                    setInterval(function () {
                      $(id + ' .OG-g-b-scroll').html(gen_rows({}));
                    }, 1000);
                });
            });
        };
    }
});