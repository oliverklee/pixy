<?php

// MAY WITH FORMALS (1), from the journal paper

a();

function a() {
    $a1 = 1;
    $a2 = 1;
    if ($GLOBALS['u']) {
        $a2 =& $a1;
    }
    ~_hotspot0;         // a.a1:1, a.a2:1
    b(&$a1);
    ~_hotspot1;         // a.a1:7, a.a2:T
}

function b(&$bp1) {
    $bp1 = 7;
}

?>
