reset
set terminal eps
set key font ",15" height 3
set xtics out font ",15"
set ytics font ",15" 
set xlabel font ",15" 
set ylabel font ",15" offset -1
set label font ",4"
set offset 0, 0.05, graph 0.25, 0.0
#set xrange [0:*]
#set yrange [0:*]
set pointsize 0.8


set output 'EnsembleTradeOff.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (s)'
plot "CommonEntry/Ensemble" using ($1):($2) title 'CommonEntry' with points pt 8 lc rgb 'red',\
"sizeBased/Ensemble" using ($1):($2) title 'sorted by dagsize' with points pt 1 lc rgb 'blue',\
"UserPreferBased/Ensemble" using ($1):($2) title 'User Preference' with points pt 2 lc rgb 'green'
set output 'other.eps'


set output 'CommonEntryPerDag.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (s)'
plot "CommonEntry/Ensemble" using ($1):($6) title 'dag1' with points pt 8 lc rgb 'red',\
"CommonEntry/Ensemble" using ($1):($10) title 'dag2' with points pt 1 lc rgb 'blue',\
"CommonEntry/Ensemble" using ($1):($14) title 'dag3' with points pt 2 lc rgb 'green',\
"CommonEntry/Ensemble" using ($1):($18) title 'dag4' with points pt 2 lc rgb 'black'
set output 'other.eps'


set output 'UserPreferBasedPerDAG.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (s)'
plot "UserPreferBased/Ensemble" using ($1):($6) title 'dag1' with points pt 8 lc rgb 'red',\
"UserPreferBased/Ensemble" using ($1):($10) title 'dag2' with points pt 1 lc rgb 'blue',\
"UserPreferBased/Ensemble" using ($1):($14) title 'dag3' with points pt 2 lc rgb 'green',\
"UserPreferBased/Ensemble" using ($1):($18) title 'dag4' with points pt 2 lc rgb 'black'
set output 'other.eps'


set output 'sizeBasedPerDAG.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (s)'
plot "sizeBased/Ensemble" using ($1):($6) title 'dag1' with points pt 8 lc rgb 'red',\
"sizeBased/Ensemble" using ($1):($10) title 'dag2' with points pt 1 lc rgb 'blue',\
"sizeBased/Ensemble" using ($1):($14) title 'dag3' with points pt 2 lc rgb 'green',\
"sizeBased/Ensemble" using ($1):($18) title 'dag4' with points pt 2 lc rgb 'black'
set output 'other.eps'
