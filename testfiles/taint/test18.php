<?php

// MAY WITH GLOBALS (1)

$evil;
$x1 = 1;
a();

function a() {
    $a1 = 1;
    if ($GLOBALS['u']) {
        $a1 =& $GLOBALS['x1'];
    }
    ~_hotspot0;     // main.x1:U/C, a.a1:U/C
    b();
    ~_hotspot1;     // main.x1:T/D, a.a1:T/D
}

function b() {
    $GLOBALS['x1'] = $GLOBALS['evil'];
}

?>
