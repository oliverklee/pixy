<? //

// demo for harmless indirect taint
$a = mysql_real_escape_string($get_a);
mysql_query("x '$a' y");


// demo for dangerous indirect taint
$a = mysql_real_escape_string($get_a);
mysql_query("x $a y");



// demo for two different taint levels
$a = mysql_real_escape_string($get_a);
$b = $get_b;
mysql_query("x $a y $b z");







?>
