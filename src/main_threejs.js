const THREE = require('three');

var windowHalfX = window.innerWidth / 2;
var windowHalfY = window.innerHeight / 2;

var camera, scene, renderer, container, stats;
var mouseX = 0, mouseY = 0;

let mesh;

function init() {
    container = document.getElementById( 'game' );

    camera = new THREE.PerspectiveCamera(20, window.innerWidth / window.innerHeight, 1, 10000);
    camera.position.z = 1800;

    scene = new THREE.Scene();
    // scene.background = new THREE.Color( 0xffffff );

    var light, object;
    var ambientLight = new THREE.AmbientLight(0xcccccc, 0.4);
    scene.add(ambientLight);
    var pointLight = new THREE.PointLight(0xffffff, 0.8);
    camera.add(pointLight);
    scene.add(camera);

    var faceIndices = [ 'a', 'b', 'c' ];
    var color, f, f2, f3, p, vertexIndex, radius = 200;

    let geometry  = new THREE.IcosahedronGeometry( radius, 1 );


    for ( var i = 0; i < geometry.faces.length; i ++ ) {
        f  = geometry.faces[ i ];
        // f2 = geometry2.faces[ i ];
        // f3 = geometry3.faces[ i ];
        for ( var j = 0; j < 3; j ++ ) {
            vertexIndex = f[ faceIndices[ j ] ];
            p = geometry.vertices[ vertexIndex ];
            color = new THREE.Color( 0xffffff );
            color.setHSL( ( p.y / radius + 1 ) / 2, 1.0, 0.5 );
            f.vertexColors[ j ] = color;
            color = new THREE.Color( 0xffffff );
            color.setHSL( 0.0, ( p.y / radius + 1 ) / 2, 0.5 );

            // f2.vertexColors[ j ] = color;
            // color = new THREE.Color( 0xffffff );
            // color.setHSL( 0.125 * vertexIndex / geometry.vertices.length, 1.0, 0.5 );
            // f3.vertexColors[ j ] = color;
        }
    }

    var wireframe;
    var material = new THREE.MeshPhongMaterial( { color: 0xffffff, flatShading: true, vertexColors: THREE.VertexColors, shininess: 0 } );
    var wireframeMaterial = new THREE.MeshBasicMaterial( { color: 0x000000, wireframe: true, transparent: true } );
    mesh = new THREE.Mesh( geometry, material );
    wireframe = new THREE.Mesh( geometry, wireframeMaterial );
    mesh.add( wireframe );
    mesh.position.x = 0;
    mesh.rotation.x = - 1.87;
    scene.add( mesh );

    renderer = new THREE.WebGLRenderer( { antialias: false } );
    renderer.setPixelRatio( window.devicePixelRatio );
    renderer.setSize( window.innerWidth, window.innerHeight );
    container.appendChild( renderer.domElement );

    stats = new Stats();
    container.appendChild( stats.dom );

    let loadingLabel = document.getElementById("loadingLabel");
    loadingLabel.style.display = "none";

    document.addEventListener( 'mousemove', onDocumentMouseMove, false );
    window.addEventListener( 'resize', onWindowResize, false );
}

function onWindowResize() {
    windowHalfX = window.innerWidth / 2;
    windowHalfY = window.innerHeight / 2;
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize( window.innerWidth, window.innerHeight );
}
function onDocumentMouseMove( event ) {
    mouseX = ( event.clientX - windowHalfX );
    mouseY = ( event.clientY - windowHalfY );
}

function render() {
    // camera.position.x += ( mouseX - camera.position.x ) * 0.05;
    // camera.position.y += ( - mouseY - camera.position.y ) * 0.05;
    // camera.lookAt( scene.position );

    mesh.rotation.x += 0.01;
    mesh.rotation.y += 0.01;

    renderer.render( scene, camera );
}

function animate() {
    requestAnimationFrame( animate );
    render();
    stats.update();
}

init();
animate();