/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.util.ui.tooltip',
    dependencies: [],
    obj: function () {
        return function (selector) {
            if (typeof selector !== 'string') throw new TypeError();
            selector = selector || 'body';
            /* Create a new tooltip if none already exists */
            if (!$('.OG-tooltip').length) $('body').append('<span class="OG-tooltip OG-shadow"></span>');
            var $tooltip = $('.OG-tooltip');
            /* Populate and display on hover */
            $(selector).find('[data-tooltip]').hover(function (e) {
                var $offset = $(this).offset(), $e_target = $(e.currentTarget), tip = $e_target.attr('data-tooltip');
                $(e.currentTarget).hasClass('OG-disabled') ? $tooltip.addClass('OG-disabled') :
                    $tooltip.removeClass('OG-disabled');
                if (tip) $tooltip.html(tip).css({
                    left: $offset.left - ($tooltip.width() / 2 - ($(e.currentTarget).innerWidth() / 3)) + 'px',
                    top: $offset.top + 30 + 'px'
                }).show();
            });
            $('[data-tooltip]').unbind('mouseleave').bind('mouseleave', function () {$tooltip.empty().hide();});
        };
    }
});