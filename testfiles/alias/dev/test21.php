<?php

// variable variables not supported

$$x =& $y;
~_hotspot0;     // u{} a{}

$y =& $$x;
~_hotspot1;     // u{} a{}

unset($$x);
~_hotspot2;     // u{} a{}


?>
