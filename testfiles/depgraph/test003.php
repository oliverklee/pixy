<?php

// modification of source variables

$a = $_GET['x'];
$_GET['x'] = 'hi';     // untainted (String constant)
$b = $_GET['x'];
echo($a);
echo($b);      // untainted (String constant)


?>
