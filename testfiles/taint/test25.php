<?php

// return values (1)

$x1 = a();
~_hotspot0;     // main.x1:U/C
$x2 = b();
~_hotspot1;     // main.x2:T/D

function a() {
    return 1;
}

function b() {
    return $GLOBALS['evil'];
}

?>