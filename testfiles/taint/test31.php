<?php

// default parameters

a();
b($evil1, $evil2);

function a($ap1 = 1) {
    ~_hotspot0;     // a.ap1:U/C
                    // note: parameters initialized by default are
                    // always U/C
}

function b($bp1, $bp2 = 2, $bp3 = 3, $bp4 = 4) {
    ~_hotspot1;     // b.bp1:T/D, b.bp2:T/D, b.bp3:U/C, b.bp4:U/C
}

?>