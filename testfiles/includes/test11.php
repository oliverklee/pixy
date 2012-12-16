<?

// was a bug that resulted in an infinite loop

$x = 'asfsdfsd';
include $x;
include $unknown;
// and yet, we can reach the following line:
echo $x;


?>
