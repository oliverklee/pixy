<? //

// nice: now we also know which variables are really tainted
// in a combined echo (simple taint analysis had problems because
// of info loss due to temporary variables)

$a = $get1;
$b = $get2;
$c = 'const';
echo($a . $b . $c); 




?>
