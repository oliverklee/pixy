<?php

// "global" statement

$x1 = 1;
a();

function a() {
    global $x1;
    ~_hotspot0;     // main.x1:1, a.x1:1
    $x1 = 2;
    ~_hotspot1;     // main.x1:2, a.x1:2
    $GLOBALS['x1'] = 3;
    ~_hotspot2;     // main.x1:3, a.x1:3
}


?>
