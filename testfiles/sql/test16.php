<? //

// concat: prefixing something unknown
$a = $get . 'x';
mysql_query($a);


// concat: embedding something unknown
$a = 'x' . $get . 'y';
mysql_query($a);


// concat: postfixing with something unknown
$a = 'x' . 'y' . $get;
mysql_query($a);







?>
