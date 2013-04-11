<?php

// some basic must-tests

~_hotspot0;     // u{} a{}
$a =& $b;
~_hotspot1;     // u{(a,b)} a{}
$c =& $a;
~_hotspot2;     // u{(a,b,c)} a{}
$d =& $b;
~_hotspot3;     // u{(a,b,c,d)} a{}
$e =& $f;
~_hotspot4;     // u{(a,b,c,d) (e,f)} a{}
$d =& $f;
~_hotspot5;     // u{(a,b,c) (d,e,f)} a{}

?>