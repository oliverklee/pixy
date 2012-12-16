<? //

// was a bug (didn't consider the possibility of uninitialized variables)
if ($get) {
    $x = 'a';
}
mysql_query($x);      // $x is either "a" or undefined






?>
