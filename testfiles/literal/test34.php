<?php

// call-by-reference with arrays:
// although not supported, make sure that at least the call-by-value effect is
// handled correctly

a();

function a() {
    $a1[1] = 1;
    b(&$a1);
}

function b(&$bp1) {
    $b2 = $bp1[1];
    ~_hotspot0;     // b.b2:1
}



?>
