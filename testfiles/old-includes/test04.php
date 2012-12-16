<?php

// non-literal include contains a function that taints a global variable

$includeMe = 'test04a.php';
$x1 = 'good';
include $includeMe;  // taints $x1
taintTheGlobal();
~_hotspot0;     // x1:Tainted/Dirty

?>
