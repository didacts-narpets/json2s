$(document).ready(function() {
//   $("#scala-code").addClass("prettyprint");
//   $("#scala-code").addClass("prettyprinted");
    $("#scala-code .typ").each(function(index) {
        console.log("Processing " + $(this).text())
        $(this).data("curr", $(this).text());
    });
    $("#scala-code .classname .typ").each(function(index) {
        var $classname = $(this);
        $classname.editable({
            success: function(response, newValue) {
                console.log(newValue);
                var prevValue = $classname.data('curr');
                console.log("Changing all " + prevValue);
                $("#scala-code .typ").each(function(index) {
                    console.log("$(this).data('curr') == " + $(this).data('curr'));
                    if ($(this).data("curr") == prevValue) {
                        $(this).data("curr", newValue);
                        $(this).text(newValue);
                    }
                });
            }
        });
    });
});
