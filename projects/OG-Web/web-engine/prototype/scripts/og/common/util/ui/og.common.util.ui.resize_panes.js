/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 *
 * Draggable resize bar that sits between two elements that require resizing.
 * The resize bar should sit in one of the other elements.
 * Stores the new size of each panel in localStorage
 *
 * @param {String} left_pane CSS selector
 * @param {String} right_pane CSS selector
 * @param {String} resize_bar CSS selector
 *
 * TODO: Double click to set to go back to default
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
                        $tmp_bar.css('left', page_x - 4 + 'px');
                    };
                $rb.css({'visibility': 'hidden'});
                $lp.disableSelection(); // Undocumented JQuery UI method
                $rp.disableSelection();
                rb_width = $rb.width();
                rb_height = $rb.height();
                $rb_position = $rb.offset();
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
                    position: 'absolute',
                    'margin-left': rb_margin_left, 'margin-right': rb_margin_right,
                    width: rb_width, height: rb_height,
                    left: rb_left, top: rb_top
                }).addClass('OG-resizeBar-active');
                $body.one('mouseup', function () {
                    localStorage['resize_panes_' + left_pane] = percentage_left + '%';
                    localStorage['resize_panes_' + right_pane] = percentage_right + '%';
                    $body.unbind('mousemove', move_panes);
                    $rb.css({'visibility': 'visible'});
                    $lp.enableSelection(); // Undocumented JQuery UI method
                    $rp.enableSelection();
                    $('.og-js-rb').remove();
                    $('.og-js-glass-pane').remove();
                });
            });
            /**
             * Initial Load
             */
            if (localStorage['resize_panes_' + left_pane] && localStorage['resize_panes_' + right_pane]) {
                $lp.width(localStorage['resize_panes_' + left_pane]);
                $rp.width(localStorage['resize_panes_' + right_pane]);
            }
        };
    }
});