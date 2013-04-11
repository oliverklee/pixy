<?php

// non-literal include must inherit the taint mappings

$includeMe = 'test03a.php';
$x1 = 'good';
$x2 = $evil;
include $includeMe;

?>