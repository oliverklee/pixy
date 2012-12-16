<?php

// "global" statement (1)

$x1 = 1;
$evil;
a();

function a() {
    global $x1;
    ~_hotspot0;     // main.x1:U/C, a.x1:U/C
    $x1 = $GLOBALS['evil'];
    ~_hotspot1;     // main.x1:T/D, a.x1:T/D
    $GLOBALS['x1'] = 1;
    ~_hotspot2;     // main.x1:U/C, a.x1:U/C
}


?>
