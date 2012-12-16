<? //

// builtin multi-dependency function

$x = explode('_', $get);
mysql_query($x[0]);

$y = explode('_', 'harmless');
mysql_query($y[0]);






?>
