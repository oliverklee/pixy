<?php

// MAY WITH GLOBALS (3)

$x1 = 1;
$x2 = 1;
a();

function a() {
    $a1 = 1;
    if ($GLOBALS['u']) {
        $a1 =& $GLOBALS['x1'];
    }
    if ($GLOBALS['v']) {
        $a1 =& $GLOBALS['x2'];
    }
    ~_hotspot0;     // main.x1:1, main.x2:1, a.a1:1
    b();
    ~_hotspot1;     // main.x1:1, main.x2:1, a.a1:1
}

function b() {
    $GLOBALS['x1'] = 1;
    $GLOBALS['x2'] = 1;
}

?>
