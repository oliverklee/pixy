<?php

// default mappings (we always assume that register_globals is active);
// {...} ExTaintSet
// (...) ExTaint
// (var,line)
// {(u)}: ExTaintSet.UNINIT; short: u
// {(h)}: ExTaintSet.HARMLESS; short: h

$x1;
$x2['1'];
~_hotspot0;     // main.x1:u/u, main.x2[1]:u/u


?>