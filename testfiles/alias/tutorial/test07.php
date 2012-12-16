<?php

a();

function a() {
    $a1; $a2;
    if ($u) {
        $a1 =& $a2;
    }
    b(&$a1, &$a2);
}

function b(&$bp1, &$bp2) {  
    ~_hotspot0;             // u{ (b.bp1, b.bp1_fs) (b.bp2, b.bp2_fs) }
                            // a{ (b.bp1, b.bp2) (b.bp1, b.bp2_fs) (b.bp2, b.bp1_fs) (b.bp1_fs, b.bp2_fs) }

}


?>
