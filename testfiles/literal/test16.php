<?php

// FORMAL MUST, with shadow-explaining redirection

a();

function a() {
    $a1 = 1;
    b(&$a1);
    ~_hotspot0;     // a.a1:1
}

function b(&$bp1) {
    $b2 = 2;
    $bp1 =& $b2;
    $bp1 = 3;
}

?>
