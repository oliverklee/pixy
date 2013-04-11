<?php

// array(): no effect on alias information

$y = 7;
$x =& $y;
$x = array();
~_hotspot0;     // u{ (main.x main.y) } a{ }

echo $y;        // $y is now an array, too
$y = 9;
echo $x;        // $x is now == 9, too


?>