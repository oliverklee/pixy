<?php

a();
~_hotspot0;         // u{} a{ (main.x1, main.x2) }

function a() {
    $a1 =& $GLOBALS['x1'];
    b();
    ~_hotspot1;             // u{ (a.a1, main.x1, a.x1_gs) }
                            // a{ (main.x2, main.x1)
                            //    (main.x2, a.a1)
                            //    (main.x2, a.x1_gs)
                            //    (main.x2, a.x2_gs) }
}

function b() {
    if ($u) {
        $GLOBALS['x2'] =& $GLOBALS['x1'];
    }
    ~_hotspot2;             // u{ (main.x1, b.x1_gs) }
                            // a{ (main.x1, main.x2) (main.x2, b.x1_gs) (main.x2, b.x2_gs) }
}
?>