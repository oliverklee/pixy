<? //


$a = array();
$b = array();
$c[2] = 'hi';
$b[7] = $c;
$a[1] = $b[7];
echo $a[1][2];   // untainted: 'hi'
?>