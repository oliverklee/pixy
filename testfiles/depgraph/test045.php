<? //

// builtin function: multi-dependency


$replacement = 'const1';
//$replacement = $get1;
//$string = 'const2';
$string = $get2;
$a = ereg_replace('findme', $replacement, $string);
echo $a;




?>