$(document).ready(function() {
    $scala = $("#scala-code");
    $scala.html(prettyPrintOne($scala.html()));
    $scala.addClass("prettyprinted");
    $scala.addClass("prettyprint");
    $("#scala-code .typ").each(function(index) {
        $(this).data("curr", $(this).text());
    });
    $("#scala-code .classname .typ").each(function(index) {
        var $classname = $(this);
        $classname.editable({
            success: function(response, newValue) {
                console.log(newValue);
                var prevValue = $classname.data('curr');
                $("#scala-code .typ").each(function(index) {
                    if ($(this).data("curr") == prevValue) {
                        $(this).data("curr", newValue);
                        $(this).text(newValue);
                    }
                });
            }
        });
    });
});
