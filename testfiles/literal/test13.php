<?php

// GLOBAL MUST, without shadow-explaining redirection

$x1 = 1;
a();

function a() {
    $a1 =& $GLOBALS['x1'];
    b();
    ~_hotspot0;         // main.x1:2, a.a1:2
}

function b() {
    $GLOBALS['x1'] = 2;
}


?>

