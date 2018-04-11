import LinkedList from "./LinkedList";
import TestClass from "./TestClass";

const THREE = require('three');

const vec3 = new THREE.Vector3();

let linkedList = new LinkedList();
let arrayList = [];

const LEN = 6000000;

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function bench() {
    console.log("start benchmark ========");
    linkedList = new LinkedList();
    arrayList = [];

    let t;
    t = new Date().getTime();
    for (let i = 0; i < LEN; i++) {
        // let id = "id-"+i;
        let o = new TestClass();
        // console.log(o);
        linkedList.addToHead(o);
        // await sleep(2000);
    }
    console.log("linked list fill=" + (new Date().getTime() - t));

    t = new Date().getTime();
    for (let i = 0; i < LEN; i++) {
        // let id = "id-"+i;
        let o = new TestClass();
        // console.log(o);
        arrayList.push(o);
        // arrayList.push(id);
    }
    console.log("array list fill=" + (new Date().getTime() - t));


    let it;

    it = 0;
    t = new Date().getTime();
    for (let i = 0; i < arrayList.length; i++) {
        it++;
        arrayList[i].someValue += i;
    }
    console.log("array iter for=" + (new Date().getTime() - t));

    it = 0;
    t = new Date().getTime();
    for (let i = arrayList.length-1; i >=0 ; i--) {
        it++;
        arrayList[i].someValue += i;
    }
    console.log("array iter for dec=" + (new Date().getTime() - t));

    it = 0;
    t = new Date().getTime();
    // const l = arrayList.length;
    for (let l = arrayList.length, i = 0; i < l; i++) {
        it++;
        arrayList[i].someValue += i;
    }
    console.log("array iter for const l=" + (new Date().getTime() - t));


    it = 0;
    t = new Date().getTime();
    let i = 0;
    const ll = arrayList.length;
    while (i < ll) {
        it++;
        arrayList[i].someValue += i;
        i++;
    }
    console.log("array iter while=" + (new Date().getTime() - t));


    it = 0;
    t = new Date().getTime();
    arrayList.forEach(o => {
        // console.log(o);
        it++;
        o.someValue += it;
    });
    console.log("array iter forEach=" + (new Date().getTime() - t));

    it = 0;
    t = new Date().getTime();
    let node = linkedList._head;
    while (node) {
        it++;
        node.value.someValue += it;
        node = node.next;
    }
    console.log("linked iter=" + (new Date().getTime() - t));
    // console.log(linkedList);


}

window.bench = bench;
bench();