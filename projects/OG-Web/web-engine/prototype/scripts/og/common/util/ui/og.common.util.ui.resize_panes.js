/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 *
 * Draggable resize bar that sits between two elements that require resizing.
 * The resize bar should sit in one of the other elements.
 * Stores the new size of each panel in localStorage 
 *
 * @param {String} leftPane CSS selector
 * @param {String} rightPane CSS selector
 * @param {String} resizeBar CSS selector 
 *
 * TODO: Doubclick to set to center (or 40/60?)
 * 
 */

$.register_module({
    name: 'og.common.util.ui.resize_panes',
    dependencies: [],
    obj: function () {
        return function (left_pane, right_pane, resize_bar) {
            var $lp = $(left_pane), $rp = $(right_pane), $rb = $(resize_bar);
            $rb.mousedown(function () {
                var $doc = $(document), $body = $('body'),
                    $rb = $(this), $rb_position, rb_width, rb_height, rb_left, rb_top, rb_margin_left, rb_margin_right, 
                    set_left_mouse_pos,
                    percentage_left, percentage_right,
                    doc_width = $doc.width(), doc_height = $doc.height(),
                    $tmp_bar = $('<div class="og-js-rb"></div>'),
                    $glass_pane = $('<div class="og-js-glass-pane"></div>'),
                    move_panes = function (e) {
                        var one_percent_in_px = doc_width / 100, page_x = e.pageX;
                        percentage_left = page_x / one_percent_in_px;
                        percentage_right = 100 - percentage_left;
                        $lp.width(percentage_left + '%');
                        $rp.width(percentage_right + '%');
                        $tmp_bar.css('left', page_x + 'px');
                    };
                $lp.disableSelection(); // Undocumented JQuery UI method
                $rp.disableSelection();
                rb_width = $rb.width() * 2;
                rb_height = $rb.height();
                $rb_position = $rb.position();
                rb_left = $rb_position.left;
                rb_top = $rb_position.top;
                rb_margin_left = $rb.css('margin-left');
                rb_margin_right = $rb.css('margin-right');
                /**
                 * The glassPane prevents background elements from interfering
                 * during the resize process
                 */
                $('body > div').after($glass_pane).next().css({
                   position: 'absolute', background: 'transparent',
                   left: '0', top: '0',
                   width: doc_width + 'px', height: doc_height + 'px'
                });
                $body.mousemove(move_panes);
                /**
                 * Temporary resizeBar that sits over the real one
                 */
                $glass_pane.after($tmp_bar).next().css({
                    position: 'absolute', 'background-color': '#ccc',
                    'margin-left': rb_margin_left, 'margin-right': rb_margin_right,
                    width: rb_width, height: rb_height,
                    left: rb_left, top: rb_top
                });
                $glass_pane.one('mouseup', function () {
                    localStorage['resize_panes_' + left_pane] = percentage_left + '%';
                    localStorage['resize_panes_' + right_pane] = percentage_right + '%';
                    $body.unbind('mousemove', move_panes);
                    $lp.enableSelection(); // Undocumented JQuery UI method
                    $rp.enableSelection();
                    $tmp_bar.remove();
                    $glass_pane.remove();
                });
            });
        };
    }
});