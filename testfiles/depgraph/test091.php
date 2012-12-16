<? //


$x = "A B $var C D";
echo $x;

$x = " A B $var C D";
echo $x;

$x = "A B C D";
echo $x;

$x = "$var";
echo $x;

$x = "";
echo $x;

$x = "   ";
echo $x;

$x = "    $var    ";  
echo $x;

$x = " A B $var";       
echo $x;

$x = `A B $var C D`;
echo $x;

$x = <<<EOD
A B
C D $var
E F
EOD;
echo $x;




?>
