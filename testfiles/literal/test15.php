<?php

// FORMAL MUST, without shadow-explaining redirection

a();

function a() {
    $a1 = 1;
    b(&$a1);
    ~_hotspot0;     // a.a1:2
}

function b(&$bp1) {
    $bp1 = 2;
}

?>
