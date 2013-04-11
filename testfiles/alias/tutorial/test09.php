<?php

a();

function a() {
    b(&$GLOBALS['x1']);
}

function b(&$bp1) {
    ~_hotspot0;         // u{ (main.x1, b.x1_gs, b.bp1, b.bp1_fs) } a{ }
}
?>