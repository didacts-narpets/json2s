$.fn.extend({
  // really simple callback validator for this specific use case
  // changes the group to either has-success or has-error
  // and enables/disables the submit button
  // and changes a help message to the result of the err callback
  validate: function(p, err) {
    this.keyup(function() {
      $parent = $(this).parent("div.form-group");
      $submit = $(this).parent("form").find("button[type=submit]");
      $help = $parent.find("span.help-block");
      if (p($(this).val())) {
        $parent.addClass("has-success");
        $parent.removeClass("has-error");
        $submit.prop("disabled", false);
        if (err) $help.text("");
      }
      else {
        $parent.removeClass("has-success");
        $parent.addClass("has-error");
        $submit.prop("disabled", true);
        if (err) $help.text(err($(this).val()));
      }
    });
  }
});

$("#class-name").validate(function(val) {
   return val.match(/^[a-zA-Z]\w*$/);
}, function(val) {
  return "Class name '" + val + "' is invalid - must match /^[a-zA-Z]\\w*$/";
});

$("#json-input").validate(function(val) {
  var json = false
  try {
    json = jQuery.parseJSON(val);
  } catch(err) {
  // todo: alert user of parse error in json
  }
  return typeof json == 'object';
}, function(val) {
 var msg = "Please enter a valid json object or array of objects"
 try {jQuery.parseJSON(val);} catch(err) {
   msg = err.message;
 }
 return msg;
});