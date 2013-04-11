<?php

// GLOBAL MUST, without shadow-explaining redirection

$x1 = 1;
$x2 = $evil;
a();

function a() {
    $a1 =& $GLOBALS['x1'];
    $a2 =& $GLOBALS['x2'];
    b();
    ~_hotspot0;         // main.x1:T/D, main.x2:U/C, a.a1:T/D, a.a2:U/C
}

function b() {
    $GLOBALS['x1'] = $GLOBALS['evil'];
    $GLOBALS['x2'] = 1;
}


?>