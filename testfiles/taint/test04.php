<?php

// simple assignments, leftCase 4


// *********************************************************************************
// left variable is an array element (and maybe an array) with *********************
// with non-literal indices                                    *********************
// *********************************************************************************

// => there are no aliases for the left variable
// => we have to test MI effects (due to non-literal indices)

// a lot of testing has already been done for literals analysis;
// works analogously (apart from caFlag)



// 1) left is not an array ------------------------------------------

$good = 1;
$evil;


$x1[1] = $good;
$x1[2] = $evil;

$x1[$i] = $good;     // should do nothing
~_hotspot0;             // x1:T/D, x1[1]:U/D, x1[2]:T/D

$x1[$j] = $evil;
~_hotspot1;             // x1:T/D, x1[1]:T/D, x1[2]:T/D


?>


