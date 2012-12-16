<?php

// MAY WITH GLOBALS, variant of (2), from the journal paper

$x1 = 1;
a();

function a() {
    $a1 = 1;
    if ($GLOBALS['u']) {
        $a1 =& $GLOBALS['x1'];
    }
    ~_hotspot0;     // main.x1:1, a.a1:1
    b();
    ~_hotspot1;     // main.x1:1, a.a1:1
}

function b() {
    $GLOBALS['x1'] = 7;
}



?>
