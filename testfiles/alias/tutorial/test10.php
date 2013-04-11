<?php

a();

function a() {
    if ($u) {
        $a1 =& $GLOBALS['x1'];
    }
    ~_hotspot0;                 // u{ (main.x1, a.x1_gs) } a{ (a.a1, main.x1) (a.a1, a.x1_gs) }
    b(&$a1);
}

function b(&$bp1) {
    ~_hotspot1;             // u{ (main.x1, b.x1_gs) (b.bp1, b.bp1_fs) }
                            // a{ (main.x1, b.bp1) (main.x1, b.bp1_fs) (b.bp1, b.x1_gs) (b.x1_gs, b.bp1_fs) }
}
?>