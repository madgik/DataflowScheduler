reset
set terminal eps
set key font ",15" height 3
set xtics out font ",15"
set ytics font ",15" 
set xlabel font ",15" 
set ylabel font ",15" offset -1
set label font ",4"
set offset 0, 0.05, graph 0.25, 0.0
set xrange [0:*]
set yrange [0:*]
set pointsize 0.8


set output 'persec_LATTICE521.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (s)'
plot "persec/LATTICE5_21/hh" using ($2/3600):($1/1000) title 'HHDS' with points pt 8 lc rgb 'red',\
"persec/LATTICE5_21/moheft" using ($2/3600):($1/1000) title 'MOHEFT' with points pt 1 lc rgb 'blue'
set output 'others.eps'

set output 'persec_LATTICE113.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (s)'
plot "persec/LATTICE11_3/hh" using ($2/3600):($1/1000) title 'HHDS' with points pt 8 lc rgb 'red',\
"persec/LATTICE11_3/moheft" using ($2/3600):($1/1000) title 'MOHEFT' with points pt 1 lc rgb 'blue'
set output 'others.eps'


set output 'persec_LIGO.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (s)'
plot "persec/LIGO/hh" using ($2/3600):($1/1000) title 'HHDS' with points pt 8 lc rgb 'red',\
"persec/LIGO/moheft" using ($2/3600):($1/1000) title 'MOHEFT' with points pt 1 lc rgb 'blue'
set output 'others.eps'

set output 'persec_MONTAGE.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (s)'
plot "persec/MONTAGE/hh" using ($2/3600):($1/1000) title 'HHDS' with points pt 8 lc rgb 'red',\
"persec/MONTAGE/moheft" using ($2/3600):($1/1000) title 'MOHEFT' with points pt 1 lc rgb 'blue'
set output 'others.eps'


set output 'perhour_LATTICE521.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "perhour/LATTICE5_21/hh" using 2:($1/60000) title 'HHDS' with points pt 8 lc rgb 'red',\
"perhour/LATTICE5_21/moheft" using 2:($1/60000) title 'MOHEFT' with points pt 1 lc rgb 'blue'
set output 'others.eps'

set output 'perhour_LATTICE113.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "perhour/LATTICE11_3/hh" using 2:($1/60000) title 'HHDS' with points pt 8 lc rgb 'red',\
"perhour/LATTICE11_3/moheft" using 2:($1/60000) title 'MOHEFT' with points pt 1 lc rgb 'blue'
set output 'others.eps'

set output 'perhour_LIGO.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "perhour/LIGO/hh" using 2:($1/60000) title 'HHDS' with points pt 8 lc rgb 'red',\
"perhour/LIGO/moheft" using 2:($1/60000) title 'MOHEFT' with points pt 1 lc rgb 'blue'
set output 'others.eps'

set output 'perhour_MONTAGE.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "perhour/MONTAGE/hh" using 2:($1/60000) title 'HHDS' with points pt 8 lc rgb 'red',\
"perhour/MONTAGE/moheft" using 2:($1/60000) title 'MOHEFT' with points pt 1 lc rgb 'blue'
set output 'others.eps'


set xrange [*:*]
set yrange [*:*]


set output 'perK_LIGO.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "perK/k10/LIGO/hh" using 2:($1/60000) title 'k=10' with points pt 8 lc rgb 'red',\
"perK/k20/LIGO/hh" using 2:($1/60000) title 'k=20' with points pt 1 lc rgb 'blue',\
"perK/k30/LIGO/hh" using 2:($1/60000) title 'k=30' with points pt 2 lc rgb 'green'
set output 'others.eps'

set output 'perK_MONTAGE.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "perK/k10/MONTAGE/hh" using 2:($1/60000) title 'k=10' with points pt 8 lc rgb 'red',\
"perK/k20/MONTAGE/hh" using 2:($1/60000) title 'k=20' with points pt 1 lc rgb 'blue',\
"perK/k30/MONTAGE/hh" using 2:($1/60000) title 'k=30' with points pt 2 lc rgb 'green'
set output 'others.eps'


set output 'perprun_MONTAGE.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "perpruning/MONTAGE/der" using 2:($1/60000) title 'knee' with points pt 8 lc rgb 'red',\
"perpruning/MONTAGE/crowd" using 2:($1/60000) title 'crowding distance' with points pt 1 lc rgb 'blue',\
"perpruning/MONTAGE/dom" using 2:($1/60000) title 'dominance' with points pt 2 lc rgb 'green'
set output 'others.eps'

set output 'perprun_LIGO.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "perpruning/LIGO/der" using 2:($1/60000) title 'knee' with points pt 8 lc rgb 'red',\
"perpruning/LIGO/crowd" using 2:($1/60000) title 'crowding distance' with points pt 1 lc rgb 'blue',\
"perpruning/LIGO/dom" using 2:($1/60000) title 'dominance' with points pt 2 lc rgb 'green'
set output 'others.eps'

set output 'hhExampleLigo.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "hhExample/LIGO/small" using 2:($1/60000) title 'small VMs' with points pt 8 lc rgb 'red',\
"hhExample/LIGO/large" using 2:($1/60000) title 'large VMs' with points pt 1 lc rgb 'blue',\
"hhExample/LIGO/hetero" using 2:($1/60000) title 'heterogeneous VMs' with points pt 2 lc rgb 'green'
set output 'others.eps'

set output 'hhExampleMontage.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "hhExample/MONTAGE/small" using 2:($1/60000) title 'small VMs' with points pt 8 lc rgb 'red',\
"hhExample/MONTAGE/large" using 2:($1/60000) title 'large VMs' with points pt 1 lc rgb 'blue',\
"hhExample/MONTAGE/hetero" using 2:($1/60000) title 'heterogeneous VMs' with points pt 2 lc rgb 'green'
set output 'others.eps'




