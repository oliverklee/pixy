<?php

// direct recursion

a(7, 8);

function a($ap1, $ap2) {

    ~_hotspot0;             
    // 1st context:  u{ (a.ap1, a.ap1_fs) (a.ap2 a.ap2_fs) }  a{ }
    // 2nd context:  u{ (a.ap1, a.ap2, a.ap1_fs, a.ap2_fs) }  a{ }
    
    if ($u) {
        a(&$ap1, &$ap1);
    }
}


?>
