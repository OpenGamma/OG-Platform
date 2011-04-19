$.register_module({
    name: 'og.views.common.layout',
    dependencies: [],
    obj: function () {
        return function (layout) {
            if (layout === 'analytics') {
                $('#OG-details, #OG-sr').hide();
                $('#OG-analytics').show();
            }
            if (layout === 'default') {
                $('#OG-details, #OG-sr').show();
                $('#OG-analytics').hide();
            }
        }
    }
});