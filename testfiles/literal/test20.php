<?php

// MAY WITH GLOBALS (1)

$x1 = 1;
a();

function a() {
    $a1 = 1;
    if ($GLOBALS['u']) {
        $a1 =& $GLOBALS['x1'];
    }
    ~_hotspot0;     // main.x1:1, a.a1:1
    b();
    ~_hotspot1;     // main.x1:2, a.a1:T
}

function b() {
    $GLOBALS['x1'] = 2;
}

?>
