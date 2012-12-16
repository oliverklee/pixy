<?php

// non-literal include defines a tainted constant

$includeMe = 'test05a.php';
include $includeMe;  // taints C1
$x1 = C1;
echo $x1;

?>
