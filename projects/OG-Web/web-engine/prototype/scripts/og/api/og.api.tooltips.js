$.register_module({
    name: 'og.api.tooltips',
    dependencies: [],
    obj: function () {
        return {
            '1': 'If checked, this view will start running when the engine starts even if a user does '+
                 'not load it in a client.',
            '2': 'A list of named <strong>column sets</strong>. These allow the same analytics '+
                 'to be computed using different settings and viewed side by side at the same time'
        }
    }
});