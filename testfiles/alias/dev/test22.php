<?php

// member variables are not supported

$x->field =& $y;
~_hotspot0;         // u{} a{}

$y =& $x->field;
~_hotspot1;         // u{} a{}

unset($x->field);   
~_hotspot2;         // u{} a{}

?>
