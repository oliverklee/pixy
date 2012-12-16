<?php

// GLOBAL MUST, with shadow-explaining redirection

$x1 = 1;
$x2 = 2;
a();

function a() {
    $a1 =& $GLOBALS['x1'];
    b();
    ~_hotspot0;         // main.x1:3, main.x2:3, a.a1:1
}

function b() {
    $GLOBALS['x1'] =& $GLOBALS['x2'];
    $GLOBALS['x1'] = 3;
}



?>

