<?php

// MUST HAS PRIORITY OVER MAY (GLOBALS)

$x1 = 1;
$x2 = 1;
a();

function a() {
    $a1 =& $GLOBALS['x1'];
    if ($GLOBALS['u']) {
        $GLOBALS['x2'] =& $GLOBALS['x1'];   // now, a1 is a must-alias of main.x1 and a may-alias of main.x2
    }
    ~_hotspot0;     // main.x1:U/C, main.x2:U/C, a.a1:U/C
    b();
    ~_hotspot1;     // main.x1:U/C, main.x2:T/D, a.a1:U/C
}

function b() {
    $GLOBALS['x2'] = $GLOBALS['dirty'];
    $GLOBALS['x1'] = 1;
    // for a.a1, only the value of its must-alias main.x1 is of importance;
    // we don't care about the value of its may-alias main.x2
}

?>