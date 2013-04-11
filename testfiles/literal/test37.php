<?php

// superglobals propagation

$_GET['y'] = 1;
$_GET['z'] = 2;
a();
~_hotspot0;             // GET[y]:3, GET[z]:5

function a() {
    ~_hotspot1;         // GET[y]:1, GET[z]:2
    $_GET['y'] = 3;
    $_GET['z'] = 4;
    ~_hotspot2;         // GET[y]:3, GET[z]:4
    b();
    ~_hotspot3;         // GET[y]:3, GET[z]:5
}

function b() {
    $_GET['z'] = 5;
    ~_hotspot4;         // GET[y]:3, GET[z]:5
}


?>