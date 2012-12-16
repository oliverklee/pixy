<?php

// literal include taints a variable

$x1 = 'good';
include 'test01a.php';  // taints $x1
~_hotspot0;     // x1:Tainted/Dirty

?>
