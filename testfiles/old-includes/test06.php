<?php

// non-literal include has a function that returns an untainted value

$includeMe = 'test06a.php';
include $includeMe;
$x1 = foo();
~_hotspot0;     // x1:U/C

?>
