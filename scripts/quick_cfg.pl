#!/usr/bin/perl

use File::Basename;
use File::Path;


my $mypath = dirname($0);
my $pixy_home = "${mypath}/..";
my $graphpath="${pixy_home}/graphs";
rmtree($graphpath, 0, 1);
mkdir($graphpath);

require("$pixy_home/config/mem.pl");

my $classpath="${pixy_home}/lib${classpath_separator}${pixy_home}/build/class";

system("java -Xms$mem_min -Xmx$mem_max -Dpixy.home=\"${pixy_home}\" -classpath \"${classpath}\" at.ac.tuwien.infosys.www.pixy.Checker -a -c @ARGV");

opendir(GRAPHSDIR, $graphpath) || die "Cannot open $graphpath directory.";
foreach my $graph (readdir(GRAPHSDIR)) {
    if (-f "$graphpath/$graph" && "$graphpath/$graph" =~ /.*\.dot/) {
#       system("dot -Tps $graphpath/$graph -o $graphpath/$graph.ps");
#       system("gv $graphpath/$graph.ps &");
       system("dotty $graphpath/$graph");
    }
}
closedir(GRAPHSDIR);
  

