<? //

$a = "./";
mysql_query($a);

$a = "'";
mysql_query($a);

$a = '"';
mysql_query($a);

// was a bug in Literal.<init>
$a = "A{$tb_id}',B";
mysql_query($a);


// was a bug in Literal.<init>, TacConverter.encapsListHelper()

$a = "A{$tb_id}''B";
mysql_query($a);






?>
