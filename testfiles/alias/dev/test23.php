<?php

// unknown functions must not change the aliasing information;
// if you no longer ignore unknown functions, deactivate this test

$x =& $y;
youDontKnowMe();
~_hotspot0;         // u{ (main.x, main.y) }  a{ }

?>
