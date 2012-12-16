#!/usr/bin/perl

use File::Basename;
$mypath = dirname($0);
$pixy_home = "${mypath}/..";

require("$pixy_home/config/mem.pl");
$classpath="${pixy_home}/lib${classpath_separator}${pixy_home}/build/class${classpath_separator}${pixy_home}/transducers/jauto-classes";

system("java -Xms$mem_min -Xmx$mem_max -Dpixy.home=\"${pixy_home}\" -classpath \"${classpath}\" at.ac.tuwien.infosys.www.pixy.Checker -a -y sql @ARGV");


