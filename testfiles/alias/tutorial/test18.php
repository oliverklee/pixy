<?php

a();
~_hotspot0;         // u{ (main.x2, main.x1) } a{ }

function a() {
    $a1 =& $GLOBALS['x1'];
    if ($u) {
        $a2 =& $GLOBALS['x1'];
    }
    ~_hotspot1;                 // u{ (main.x1, a.x1_gs, a.a1) (main.x2, a.x2_gs) }
                                // a{ (a.a2, main.x1) (a.a2, a.x1_gs) (a.a2, a.a1) }
    b();
    ~_hotspot2;                 // u{ (main.x1, main.x2, a.x2_gs) (a.a1, a.x1_gs) }
                                // a{ (a.a1, a.a2) (a.a2, a.x1_gs) }
}

function b() {
    $GLOBALS['x1'] =& $GLOBALS['x2'];
}
?>