<?php

a();
~_hotspot0;         // u{ } a{ (main.x1, main.x2) }

function a() {
    $a1 =& $GLOBALS['x1'];
    if ($u) {
        $a2 =& $GLOBALS['x1'];
    }
    b();
    ~_hotspot1;                 // u{ (a.a1, a.x1_gs) (main.x2, a.x2_gs) }
                                // a{ (a.a1, a.a2) (a.a1, main.x1)
                                //    (a.a2, main.x1) (a.a2, a.x1_gs)
                                //    (main.x1, main.x2) (main.x1, a.x2_gs) (main.x1, a.x1_gs)}

}

function b() {
    if ($u) {
        $GLOBALS['x1'] =& $GLOBALS['x2'];
    }
    ~_hotspot2;                 // u{ (main.x2, b.x2_gs) }
                                // a{ (main.x1, b.x1_gs) (main.x1, main.x2) (main.x1, b.x2_gs) }
}
?>