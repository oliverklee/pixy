<?php

// the given report should be able to tell you which call is
// responsible for foo.fp1 being tainted

foo('good');
foo('ok');
foo($evil);
foo('nice');

function foo($fp1) {
    echo $fp1;
}


?>