<?php

a();

function a() {
    if ($u) {
        $a1 =& $GLOBALS['x1'];
    }
    if ($v) {
        $a1 =& $GLOBALS['x2'];
    }
    ~_hotspot0;                 // u{ (main.x1, a.x1_gs) (main.$x2, a.x2_gs) }
                                // a{ (main.x1, a.a1) (main.x2, a.a1) (a.x1_gs, a.a1) (a.x2_gs, a.a1) }
    b(&$a1);
}

function b(&$bp1) {
    ~_hotspot1;             // u{ (main.x1, b.x1_gs) (main.x2, b.x2_gs) (b.bp1, b.bp1_fs) }
                            // a{ (b.bp1, main.x1) (b.bp1 main.x2)
                            //    (b.bp1, b.x1_gs) (b.bp1, b.x2_gs)
                            //    (b.bp1_fs main.x1) (b.bp1_fs main.x2)
                            //    (b.bp1_fs b.x1_gs) (b.bp1_fs b.x2_gs)
}
?>