<?php

// main function calling with params

a(&$x, $y);

function a(&$ap1, $ap2) {
    ~_hotspot0;             // u{ (main.y a.y_gs) (main.x a.ap1 a.x_gs a.ap1_fs) (a.ap2 a.ap2_fs) }  a{ }
}

?>