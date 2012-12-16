<? //

$a = 'a';
define('TBL', $a . 'b');
$sql = 'x' . TBL;
mysql_query($sql);






?>
