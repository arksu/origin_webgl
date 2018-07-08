import {Core} from "./core/Core";

let core = new Core();

window.onresize = function () {
    core.changeResolution(window.innerWidth, window.innerHeight);
};
document.addEventListener('DOMContentLoaded', function () {
    core.changeResolution(window.innerWidth, window.innerHeight);
});

core.start();
