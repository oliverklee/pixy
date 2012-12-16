<?php

// call-by-value with arrays:
// make sure the values for the elements are propagated as well

a();

function a() {
    $a1[1] = 1;
    b($a1);
}

function b($bp1) {
    $b2 = $bp1[1];
    ~_hotspot0;     // b.b2:1
}



?>
