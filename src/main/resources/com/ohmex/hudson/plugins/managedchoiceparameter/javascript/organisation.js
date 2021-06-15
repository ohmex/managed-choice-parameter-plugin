'use strict';
jQuery.noConflict();

var ManagedChoice = ManagedChoice || (function($) {
  var instance = {};
  var orgData;
  var productDropDown;
  var projectDropDown;
  var envDropDown;

  function clear(el, val) {
    el.empty();
    el.append('<option selected="true" disabled>--Select ' + val + '--</option>');
    el.prop('selectedIndex', 0);
  }

  function load(url, productEL, projectEL, envEL) {
    productDropDown = $(productEL);
    projectDropDown = $(projectEL);
    envDropDown = $(envEL);

    $.getJSON(url, function (data) {
      orgData = data;

      clear(productDropDown, "Product");
      clear(projectDropDown, "Project");
      clear(envDropDown, "Environment");

      $.each(orgData.products, function (index, value) {
        productDropDown.append($('<option></option>').attr('value', value.name).text(value.name + ": " + value.desc));
      });

      productDropDown.change(function () {
        let selProduct = this.options[this.selectedIndex].value;

        clear(projectDropDown, "Project");
        clear(envDropDown, "Environment");

        var productData = orgData.products.find(obj => {
          return obj.name === selProduct
        });

        $.each(productData.projects, function (index, value) {
          projectDropDown.append($('<option></option>').attr('value', value.name).text(value.name + ": " + value.desc));
        });
      });

      projectDropDown.change(function () {
        let selProduct = productDropDown.find(":selected").val();
        let selProject = this.options[this.selectedIndex].value;

        clear(envDropDown, "Environment");

        var productData = orgData.products.find(obj => {
          return obj.name === selProduct
        });

        var envData = productData.projects.find(obj => {
          return obj.name === selProject
        });

        $.each(envData.env, function (index, value) {
          envDropDown.append($('<option></option>').attr('value', value.ext).text(value.desc));
        });
      });

      envDropDown.change(function () {
        let selProduct = productDropDown.find(":selected").val();
        let selProject = projectDropDown.find(":selected").val();
        let selEnv = this.options[this.selectedIndex].value;

        // Enable Build Now
      });
    });
  }

  instance.load = load;
  return instance;
})(jQuery);