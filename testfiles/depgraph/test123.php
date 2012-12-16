<? //

// simulating builtin stuff during literals analysis:
// - realpath
// - dirname
// and __FILE__


require_wrapper(realpath(dirname(__FILE__)) . '/test123b.php'); 

function require_wrapper($x) {
    require($x);
}

?>
