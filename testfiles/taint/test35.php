<?php

// when allowing unknown functions, their
// return value should be tainted/dirty (sound)

$x1 = 1;
$x1 = youDontKnowMe();
~_hotspot0;         // x1:T/D

?>