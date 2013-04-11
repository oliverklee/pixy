<?php

a();
~_hotspot0;       // u{ } a{ (main.x1, main.x2) }

function a() {
    $a1 =& $a3;
    if ($u) {
        $a2 =& $a3;
    }
    b(&$a3);
    ~_hotspot1;                 // u{ (a.a1, a.a3, main.x1) }
                                // a{ (a.a2, a.a1) (a.a2, a.a3) (a.a2, main.x1)
                                //    (main.x1, main.x2)
                                //    (main.x2, a.x2_gs) (main.x2, a.a1) (main.x2, a.a2) (main.x2, a.a3) }

}

function b(&$bp) {
    $GLOBALS['x1'] =& $bp;
    if ($u) {
        $GLOBALS['x2'] =& $bp;
    }
}

?>