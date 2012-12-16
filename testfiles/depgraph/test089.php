<? //

// ... doing something to sanitize / validate $evil ...
// so use Pixy's suppression function to eliminate the false positive
$evil = pixy_sanit();

echo $evil;     // false positive!


?>
