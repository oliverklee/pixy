<?php

// non-literal include taints a variable

$includeMe = 'test02a.php';
$x1 = 'good';
include $includeMe;  // taints $x1
~_hotspot0;     // x1:Tainted/Dirty

?>