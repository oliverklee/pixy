<?php

// AssignUnary, AssignBinary

$a = -$b;
~_hotspot0;  // $a is untainted

$a = $b;
~_hotspot1;  // $a is tainted

$a = $b + $c;
~_hotspot2;  // $a is untainted

// ~_retCa
// NEED TO IMPLEMENT FUNCTION CALLS FIRST

// $x = mysql_fetch_row($b);
~_hotspot3;  // $x is untainted

$y = isset($b);
~_hotspot4;  // $y is untainted



?>