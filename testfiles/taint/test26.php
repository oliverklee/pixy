<?php

// return values (2)
// when returning arrays, precision is lost in case of partially tainted arrays:
// the elements of the catching variable are all set to TAINTED

$x1[1]; $x1[2];
$x1 = a();
~_hotspot0;     // main.x1[1]:T/D, main.x1[2]:T/D

$x2[1]; $x2[2];
$x2 = b();
~_hotspot1;     // main.x2[1]:U/C, main.x2[2]:U/C

function a() {
    $a1 = array();
    $a1[1] = $GLOBALS['evil'];
    $a1[2];
    ~_hotspot2;     // a.a1[1]:T/D, a.a1[2]:U/D
    return $a1;
}

function b() {
    $b1 = array();
    $b1[1]; $b1[2];
    return $b1;
}

?>
