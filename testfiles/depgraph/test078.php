<? //

// disabled alias analysis:
// example for a case where a simple globals replacement
// leads to wrong results (very rare)

$x = $evil;
foo($evil);
echo $x;        // still evil!

function foo($fp) {
    if (false) {
        global $x;  // conditional "global" declaration, very rare
    }
    $x = 1;     // this overwrites the local, not the global
}

?>