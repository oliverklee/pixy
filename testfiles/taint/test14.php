<?php

// FORMAL MUST, with shadow-explaining redirection

$evil;
a();

function a() {
    $a1 = $GLOBALS['evil'];
    ~_hotspot0;     // a.a1:T/D
    b(&$a1);
    ~_hotspot1;     // a.a1:T/D
}

function b(&$bp1) {
    $b2 = 1;
    $bp1 =& $b2;
    $bp1 = 2;
}

?>
