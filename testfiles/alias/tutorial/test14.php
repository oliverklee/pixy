<?php

a();
~_hotspot0;         // u{ (main.x1, main.x2) } a{ }

function a() {
    if ($u) {
        $a1 =& $GLOBALS['x1'];
    }
    b();
    ~_hotspot1;                 // u{ (main.x1, main.x2, a.x1_gs) } 
                                // a{ (a.a1, main.x1) 
                                //    (a.a1, main.x2) 
                                //    (a.a1, a.x1_gs) }
}

function b() {
    $GLOBALS['x2'] =& $GLOBALS['x1'];
}



?>
