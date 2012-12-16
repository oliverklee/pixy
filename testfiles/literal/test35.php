<?php

// PHP's array index conversion

$x1[1] = 'int-val';
$x1["1"] = 'string-val';
~_hotspot0;             // x1[1]:string-val
//var_dump($x1);           // array(1) { [1]=>  string(10) "string-val" }


$x2[1] = 'int-val';
$x2["01"] = 'string-val';
~_hotspot1;             // x2[1]:int-val, x2[01]:string-val
//var_dump($x2);           // array(2) { [1]=>  string(7) "int-val" ["01"]=>  string(10) "string-val" }


/* TODO:  
$x3[1] = 'int-val';
$x3[1.0] = 'float-val';
~_hotspot2;             // x3[1]:float-val
var_dump($x3);          // array(1) { [1]=>  string(9) "float-val" }
*/

?>
