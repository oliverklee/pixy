<?php

// define()

// case-sensitive (default param)
define('C1', '1');    
$C1 = C1;
$c1 = c1;
~_hotspot0;             // C1:1, c1:c1

// case-sensitive
define('C2', '2', false);    
$C2 = C2;
$c2 = c2;
~_hotspot1;             // C2:2, c2:c2

// case-insensitive
define('C3', '3', true);    
$C3 = C3; 
$c3 = c3;
~_hotspot2;             // C3:3, c3:3

// using variables
$x1 = 'C4';
$x2 = '4';
define($x1, $x2);
$C4 = C4;
~_hotspot3;             // C4:4

// unknown case sensitivity (1)
define('C5', '5', $u);
$C5 = C5; 
$c5 = c5;
~_hotspot4;             // C5:5, c5:T

// unknown case sensitivity (2)
define('C6', 'c6', $u);
$C6 = C6; 
$c6 = c6;
~_hotspot5;             // C6:c6, c6:c6


?>
