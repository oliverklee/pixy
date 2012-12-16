<? //

// uninitialized global variables are tainted

mysql_query('a' . $x . 'b');






?>
