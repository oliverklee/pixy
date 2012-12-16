<?php

// default parameters

a();
b(7, 8);

function a($ap1 = 1) {
    ~_hotspot0;     // a.ap1:1
}

function b($bp1, $bp2 = 2, $bp3 = 3, $bp4 = 4) {
    ~_hotspot1;     // b.bp1:7, b.bp2:8, b.bp3:3, b.bp4:4
}

?>
