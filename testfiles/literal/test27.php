<?php

// MUST HAS PRIORITY OVER MAY (GLOBALS)

$x1 = 1;
$x2 = 2;
a();

function a() {
    $a1 =& $GLOBALS['x1'];
    if ($GLOBALS['u']) {
        $GLOBALS['x2'] =& $GLOBALS['x1'];   // now, a1 is a must-alias of main.x1 and a may-alias of main.x2
    }
    ~_hotspot0;     // main.x1:1, main.x2:T, a.a1:1
    b();
    ~_hotspot1;     // main.x1:4, main.x2:T, a.a1:4
}

function b() {
    $GLOBALS['x2'] = 3;
    $GLOBALS['x1'] = 4;
    // for a.a1, only the value of its must-alias main.x1 is of importance;
    // we don't care about the value of its may-alias main.x2
}

?>
