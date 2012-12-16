<?php

// simple assignments, leftCase 3

// note that leftCase 2 is covered by this one (for literal analysis):
// no need to write separate tests for it


// *********************************************************************************
// left variable is an array element (and maybe an array) **************************
// without non-literal indices                            **************************
// *********************************************************************************

// => there are no aliases for the left variable
// => we don't have to care about MI variables
// => we have to test overlap effects


// 1) left is not an array ------------------------------------------

// right: literal
$z1[1][1] = 'a';

$zr[1][1] = 'b';      // *r*ead-only use

// right: normal variable
$x1 = 'c';
$z1[2][1] = $x1;

// right: foreign array element (and not an array)
$z1[3][1] = $zr[1][1];

// right: myself
$z1[4][1] = 'd';
$z1[4][1] = $z1[4][1];

// right: foreign array & array element
$z1[5][1] = $zr[1];


~_hotspot0;     // z1[1][1]:a, z1[2][1]:c, z1[3][1]:b, z1[4][1]:d, z1[5][1]:T


// 2) left is an array ----------------------------------------------

// we can reuse the first dimension of z1 as left variable

// right: literal
$z1[1] = 'a';

// right:  normal variable
$z1[2] = $x1;

// right: an array element (and not an array)
$z1[3] = $zr[1][1];

// right: myself
$z1[4] = 'b';
$z1[4] = $z1[4];

~_hotspot1;     // z1[1]:a, z1[1][1]:NULL, z1[2]:c, z1[2][1]:T, z1[3]:b, z1[4]:b, z1[4][1]:NULL

// right:  foreign array & array element
$z1[5][1] = 'e';
$z1[5][2] = 'f';
$z1[5] = $zr[1];

~_hotspot2;     // z1[5][1]:b, z1[5][2]:T

// right: array & array element from left's subspace
// (check if algorithm can handle simultaneous read/write to the same array)
$z3[1][1] = 'a';
$z3[1][3] = 'b';
$z3[1][2][1] = 'c';
$z3[1][2][2] = 'd';
$z3[1][2][4] = 'e';

$z3[1] = $z3[1][2];

~_hotspot3;     // z3[1][1]:c, z3[1][3]:T, z3[1][2]:d, z3[1][2][1]:T, z3[1][2][2]:T, z3[1][2][4]:T

// right: array & array element from left's hyperspace
$z4[1] = 'a';
$z4[2][1] = 'b';
$z4[2][3] = 'd';
$z4[2][2][1] = 'c';

$z4[2] = $z4;

~_hotspot4;     // z4[1]:a, z4[2][1]:a, z4[2][3]:T, z4[2][2][1]:b




?>
