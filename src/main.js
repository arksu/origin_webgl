import TestClass from "./TestClass";

const THREE = require('three');

const vec3 = new THREE.Vector3();

const test = new TestClass();
console.log(test);

console.log(vec3);

let someVar = "ok ok";

[1,2,3].map(n => n + 1);

console.log(someVar);

const arr = [1, 2, 3];
const iAmJavascriptES6 = () => console.log(...arr);
window.iAmJavascriptES6 = iAmJavascriptES6;