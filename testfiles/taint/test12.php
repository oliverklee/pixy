<?php

// GLOBAL MUST, with shadow-explaining redirection

$x1 = $evil;
$x2 = 1;
a();

function a() {
    $a1 =& $GLOBALS['x1'];
    b();
    ~_hotspot0;         // main.x1:U/C, main.x2:U/C, a.a1:T/D
}

function b() {
    $GLOBALS['x1'] =& $GLOBALS['x2'];
    $GLOBALS['x1'] = 1;
}



?>

