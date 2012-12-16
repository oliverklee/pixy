<?php

// FORMAL MUST, without shadow-explaining redirection

$evil;
a();

function a() {
    $a1 = $GLOBALS['evil'];
    ~_hotspot0;     // a.a1:T/D
    b(&$a1);
    ~_hotspot1;     // a.a1:U/C
}

function b(&$bp1) {
    $bp1 = 1;
}

?>
