/******/ (function() { // webpackBootstrap
/******/ 	"use strict";

;// ./src/main/js/util/behavior-shim.js
function specify(selector, id, priority, behavior) {
  Behaviour.specify(selector, id, priority, behavior);
}
function applySubtree(startNode, includeSelf) {
  Behaviour.applySubtree(startNode, includeSelf);
}
/* harmony default export */ var behavior_shim = ({
  specify,
  applySubtree
});
;// ./src/main/js/pages/project/build.js


/**
 * Attaches click behavior to "Build Now" buttons to trigger a POST
 * request and show a success or error notification.
 */
behavior_shim.specify("button[data-type='build-now']", "button-build-now", 999, button => {
  button.addEventListener("click", function (event) {
    let success = button.dataset.buildSuccess;
    let failure = button.dataset.buildFailure;
    fetch(button.dataset.baseUrl + button.dataset.href, {
      method: "post",
      headers: crumb.wrap({})
    }).then(rsp => {
      if (rsp.status === 201) {
        notificationBar.show(success, notificationBar.SUCCESS);
      } else {
        notificationBar.show(failure, notificationBar.ERROR);
      }
    });
    event.preventDefault();
  });
});
/******/ })()
;
//# sourceMappingURL=build.js.map