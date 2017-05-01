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
set pointsize 0.7

set output 'persec_LIGO.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "persec/LIGO/hh" using ($2/3600):($1/1000) title 'HHDS' with points pt 8 lc rgb 'red',\
"persec/LIGO/moheft" using ($2/3600):($1/1000) title 'MOHEFT' with points pt 1 lc rgb 'blue'
set output 'others.eps'

set output 'persec_MONTAGE.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "persec/MONTAGE/hh" using ($2/3600):($1/1000) title 'HHDS' with points pt 8 lc rgb 'red',\
"persec/MONTAGE/moheft" using ($2/3600):($1/1000) title 'MOHEFT' with points pt 1 lc rgb 'blue'
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
plot "perK/k10/LIGO/hh" using 2:($1/60000) title 'k=10' with points pt 4 lc rgb 'blue',\
"perK/k20/LIGO/hh" using 2:($1/60000) title 'k=20' with points pt 8 lc rgb 'green',\
"perK/k30/LIGO/hh" using 2:($1/60000) title 'k=30' with points pt 1 lc rgb 'grey'
set output 'others.eps'

set output 'perK_MONTAGE.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "perK/k10/MONTAGE/hh" using 2:($1/60000) title 'k=10' with points pt 4 lc rgb 'blue',\
"perK/k20/MONTAGE/hh" using 2:($1/60000) title 'k=20' with points pt 8 lc rgb 'green',\
"perK/k30/MONTAGE/hh" using 2:($1/60000) title 'k=30' with points pt 1 lc rgb 'grey'
set output 'others.eps'


set output 'perprun_MONTAGE.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "perpruning/MONTAGE/der" using 2:($1/60000) title 'derivative-distance' with points pt 8 lc rgb 'red',\
"perpruning/MONTAGE/crowd" using 2:($1/60000) title 'crowding distance' with points pt 1 lc rgb 'blue',\
"perpruning/MONTAGE/dom" using 2:($1/60000) title 'dominance' with points pt 2 lc rgb 'green'
set output 'others.eps'






