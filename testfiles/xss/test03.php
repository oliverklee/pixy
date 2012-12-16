<? //

// printf is a builtin function, but a sink for XSS analysis;
// check if it is correctly detected, and not lost inside a basic block




$a = 1;
$b = md5($evil);
$c = trim($evil);
$d = 1;
printf($c);     // vuln
$e = 1;
md5($c);




?>
