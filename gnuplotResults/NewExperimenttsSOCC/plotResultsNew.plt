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


set output 'perK_MONTAGE.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "perk/MONTAGE/k10" using 2:($1/60000) title 'k=10' with points pt 8 lc rgb 'red',\
"perk/MONTAGE/k20" using 2:($1/60000) title 'k=20' with points pt 1 lc rgb 'blue',\
"perk/MONTAGE/k30" using 2:($1/60000) title 'k=30' with points pt 2 lc rgb 'green'
set output 'others.eps'


set output 'perprun_MONTAGE.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "perprun/MONTAGE/der" using 2:($1/60000) title 'knee' with points pt 8 lc rgb 'red',\
"perprun/MONTAGE/crowd" using 2:($1/60000) title 'crowding distance' with points pt 1 lc rgb 'blue',\
"perprun/MONTAGE/dom" using 2:($1/60000) title 'dominance' with points pt 2 lc rgb 'green'
set output 'others.eps'

