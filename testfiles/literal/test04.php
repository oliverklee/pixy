<?php

// simple assignments, leftCase 4


// *********************************************************************************
// left variable is an array element (and maybe an array) with *********************
// with non-literal indices                                    *********************
// *********************************************************************************

// => there are no aliases for the left variable
// => we have to test MI effects (due to non-literal indices)
// => we have to test overlap effects


// 1) left is not an array ------------------------------------------

// right: literal
$z1[1][1] = 'a';
$z1[1][2] = 'b';
$z1[2][1] = 'c';
$z1[2][2] = 'd';

$z1[1][$i] = 'b';
$z1[$j][1] = 'c';

~_hotspot0;     // z1[1][1]:T
                // z1[1][2]:b
                // z1[2][1]:c
                // z1[2][2]:d
                // z1[1][$i]:T  (non-literal array elements are always T)

// right: normal variable 
// (no need to test this again: too simple)

// right: foreign array element (and not an array)
// (no need to test this again: too simple)

// right: myself
// this one's more interesting:
$z1[3][1] = 'd';
$x1 = $z1[3][$i]; 

~_hotspot1;             // x1:T


// 2) left is an array ----------------------------------------------

// and so on (hard to make anything wrong here: just replace
// strongOverlap by weakOverlap and use MI variables; 
// test05.php checks the MI algorithm)

?>





