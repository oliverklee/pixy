<?php

// overlapping deep arrays with non-literal indices

$b = array();

$a = $b;
~_hotspot1; // $a is completely untainted

// ... continue here



?>