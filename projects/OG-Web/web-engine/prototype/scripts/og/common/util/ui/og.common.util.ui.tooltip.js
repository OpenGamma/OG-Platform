/**
 * @copyright 2009 - 2011 by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.util.ui.tooltip',
    dependencies: [],
    obj: function () {
        var classes = {
            'data-attributes': ['data-tooltip-level', 'data-tooltip-location'],
            'data-tooltip-level': ['danger', 'warning', 'off'],
            'data-tooltip-location': ['top', 'bottom']
        };
        return function (selector) {
            if (typeof selector !== 'string') throw new TypeError();
            selector = selector || 'body';
            /* Create a new tooltip if none already exists */
            if (!$('.OG-tooltip').length) $('body').append('<span class="OG-tooltip OG-shadow"></span>');
            var $tooltip = $('.OG-tooltip');
            /* Populate and display on hover */
            $(selector).find('[data-tooltip]').hover(function (e) {
                var $offset = $(this).offset(), $e_target = $(e.currentTarget), tip = $e_target.attr('data-tooltip');
                /**
                 * Set up CSS classes for tooltip Style and Location
                 * Style: warning, danger...
                 * Location: top, bottom...
                 */
                classes['data-attributes'].forEach(function (val) {
                    $e_target.attr(val) ? $tooltip.addClass('og-' + $e_target.attr(val))
                        : classes[val].forEach(function (v) {$tooltip.removeClass('og-' + v);});
                });
                /**
                 * Position and display
                 * TODO: currently only supports location: top
                 */
                $tooltip.html(tip).css({
                    left: $offset.left - ($tooltip.width() / 2 - $(e.currentTarget).width() / 3) + 'px',
                    top: $offset.top - 19 + 'px'
                }).show();
            });
            $('[data-tooltip]').unbind('mouseleave').bind('mouseleave', function () {$tooltip.empty().hide();});
        };
    }
});