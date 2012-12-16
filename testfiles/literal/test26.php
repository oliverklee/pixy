<?php

// MAY WITH FORMALS (3)

a();

function a() {
    $a1 = 1;
    $a2 = 2;
    if ($GLOBALS['u']) {
        $a2 =& $a1;
    }
    ~_hotspot0;         // a.a1:1, a.a2:T
    b(&$a1);
    ~_hotspot1;         // a.a1:3, a.a2:T
}

function b(&$bp1) {
    $bp1 = 3;
}

?>
