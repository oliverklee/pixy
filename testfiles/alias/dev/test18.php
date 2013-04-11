<?php

// main function calling with cross-linked params

a(&$x, &$x);

function a(&$ap1, &$ap2) {
    ~_hotspot0;             // u{ (main.x a.ap1 a.ap2 a.x_gs a.ap1_fs a.ap2_fs) } a{ }
}

?>