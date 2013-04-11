<?php

// simple assignments, leftCase 3

// *********************************************************************************
// left variable is an array element (and maybe an array) **************************
// without non-literal indices                            **************************
// *********************************************************************************

// => there are no aliases for the left variable
// => we don't have to care about MI variables

// a lot of testing has already been done for literals analysis;
// works analogously (apart from caFlag)



// assigning something evil to an array element should
// turn the array into a dirty one

$x1 = array();
$x1[1] = $evil;
~_hotspot0;         // x1:U/D, x1[1]:T/D

// assigning something good to an element of a dirty
// array should not make it clean (although in this special
// case, a more precise analysis would be able to do so)

$x1[1] = 1;
~_hotspot1;         // x1:U/D, x1[1]:U/D



?>