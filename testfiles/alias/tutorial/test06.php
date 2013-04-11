<?php

a();

function a() {

    $a1; $a2; $a3;
    $a1 =& $a2;
    b(&$a1, &$a2, &$a3);
}

function b(&$bp1, &$bp2, &$bp3) {
    ~_hotspot0;                     // u{(b.bp1, b.bp2, b.bp2_fs, b.bp1_fs) (b.bp3, b.bp3_fs)} a{}
}
?>