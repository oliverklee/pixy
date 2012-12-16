<?php

// MAY WITH FORMALS (1)

$evil;
a();

function a() {
    $a1 = 1;
    $a2 = 1;
    if ($GLOBALS['u']) {
        $a2 =& $a1;
    }
    ~_hotspot0;         // a.a1:U/C, a.a2:U/C
    b(&$a1);
    ~_hotspot1;         // a.a1:T/D, a.a2:T/D
}

function b(&$bp1) {
    $bp1 = $GLOBALS['evil'];
}

?>
