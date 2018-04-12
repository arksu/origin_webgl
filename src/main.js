import BenchmarkLists from "./benchmark/BenchmarkLists";

const THREE = require('three');

const vec3 = new THREE.Vector3();

let benchmark = new BenchmarkLists();

window.bench = benchmark.bench;
bench();