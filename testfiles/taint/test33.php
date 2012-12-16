<?php


// check if attributes of superglobals are correctly propagated across function calls

$_GET['y'] = 1;
$_GET['z'] = 1;
a();
~_hotspot0;         // GET[y]:U/D, GET[z]:T/D

function a() {
    ~_hotspot1;         // GET[y]:U/D, GET[z]:U/D
    b();
    ~_hotspot2;         // GET[y]:U/D, GET[z]:T/D
}

function b() {
    $_GET['z'] = $GLOBALS['evil'];
}



?>

