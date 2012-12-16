<?php

// a VERY simple demo file for getting started;
// see doc/readme.txt for usage instructions;
// for more complex demos, take a look into the "testfiles" folder

$a = 'hi';
$b = $_GET['evil'];

echo $a;    // this one is OK
echo $b;    // XSS vulnerability

?>
