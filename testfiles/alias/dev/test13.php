<?php

// may-aliases between formals and globals

$x;
a();

function a() {
    if ($u) {
        global $x;
    }
    ~_hotspot0;     // 
                    // u{ (main.x, a.x_gs) }
                    // a{ (a.x, main.x) (a.x, a.x_gs) }

    $a1 =& $x;
    ~_hotspot1;     // 
                    // u{ (main.x, a.x_gs) (a.x, a.a1) }
                    // a{ (a.x, main.x) (a.x, a.x_gs) (a.a1, main.x) (a.a1, a.x_gs) }
                    
    b(&$a1);
    ~_hotspot2;     // same as at hotspot1
 
}

function b(&$bp1) {
    ~_hotspot3;     // bp1 is now a may-alias of main.x:
                    // u{ (main.x, b.x_gs) (b.bp1, b.bp1_fs) }
                    // a{ (main.x b.bp1) 
                    //    (main.x b.bp1_fs)
                    //    (b.bp1 b.x_gs)
                    //    (b.bp1_fs b.x_gs) }

    
}

?>
