<?php

// non-literal include has a function that returns an untainted value

$includeMe = 'test06a.php';
include $includeMe;
$x1 = foo();
echo $x1;

?>