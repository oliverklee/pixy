<? //

// was a bug that led to an infinite loop during analysis

foo();

function foo() {
    if (rand()) {
        foo();
    }
}





?>
