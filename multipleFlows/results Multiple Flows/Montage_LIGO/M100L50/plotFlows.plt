reset
set terminal eps
set key font ",15" height 3
set xtics out font ",15"
set ytics font ",15" 
set xlabel font ",15" 
set ylabel font ",15" offset -1
set label font ",4"
set offset 0, 0.05, graph 0.25, 0.0
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
plot "CommonEntry/Ensemble" using ($1):($6) title 'dag1-MONTAGE100' with points pt 8 lc rgb 'red',\
"CommonEntry/Ensemble" using ($1):($10) title 'dag2-LIGO50' with points pt 1 lc rgb 'blue',\
"CommonEntry/Ensemble" using ($1):($14) title 'dag3-MONTAGE100' with points pt 2 lc rgb 'green',\
"CommonEntry/Ensemble" using ($1):($18) title 'dag4-LIGO50' with points pt 2 lc rgb 'black'
set output 'other.eps'


set output 'UserPreferBasedPerDAG.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (s)'
plot "UserPreferBased/Ensemble" using ($1):($6) title 'dag1-MONTAGE100' with points pt 8 lc rgb 'red',\
"UserPreferBased/Ensemble" using ($1):($10) title 'dag2-LIGO50' with points pt 1 lc rgb 'blue',\
"UserPreferBased/Ensemble" using ($1):($14) title 'dag3-MONTAGE100' with points pt 2 lc rgb 'green',\
"UserPreferBased/Ensemble" using ($1):($18) title 'dag4-LIGO50' with points pt 2 lc rgb 'black'
set output 'other.eps'


set output 'sizeBasedPerDAG.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (s)'
plot "sizeBased/Ensemble" using ($1):($6) title 'dag1-MONTAGE100' with points pt 8 lc rgb 'red',\
"sizeBased/Ensemble" using ($1):($10) title 'dag2-LIGO50' with points pt 1 lc rgb 'blue',\
"sizeBased/Ensemble" using ($1):($14) title 'dag3-MONTAGE100' with points pt 2 lc rgb 'green',\
"sizeBased/Ensemble" using ($1):($18) title 'dag4-LIGO50' with points pt 2 lc rgb 'black'
set output 'other.eps'


set output 'slowDownPerDAGMontage.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (s)'
plot "CommonEntry/Ensemble" using ($1):(1-$6/$14) title 'CommonEntry' with points pt 8 lc rgb 'red',\
"sizeBased/Ensemble" using ($1):(1-$6/$14) title 'sorted by dagsize' with points pt 1 lc rgb 'blue',\
"UserPreferBased/Ensemble" using ($1):(1-$6/$14) title 'User Preference' with points pt 2 lc rgb 'green'
set output 'other.eps'

set output 'slowDownPerDAGLigo.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (s)'
plot "CommonEntry/Ensemble" using ($1):(1-$10/$18) title 'CommonEntry' with points pt 8 lc rgb 'red',\
"sizeBased/Ensemble" using ($1):(1-$10/$18) title 'sorted by dagsize' with points pt 1 lc rgb 'blue',\
"UserPreferBased/Ensemble" using ($1):(1-$10/$18) title 'User Preference' with points pt 2 lc rgb 'green'
set output 'other.eps'


set output 'CommonEntryDag1.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (s)'
plot "CommonEntry/Ensemble" using ($1):($6) title 'dag1' with points pt 8 lc rgb 'red'
set output 'other.eps'


set output 'CommonEntryDag2.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (s)'
plot "CommonEntry/Ensemble" using ($1):($10) title 'dag2' with points pt 8 lc rgb 'red'
set output 'other.eps'


set output 'CommonEntryDag3.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (s)'
plot "CommonEntry/Ensemble" using ($1):($14) title 'dag3' with points pt 8 lc rgb 'red'
set output 'other.eps'


set output 'CommonEntryDag4.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (s)'
plot "CommonEntry/Ensemble" using ($1):($18) title 'dag4' with points pt 8 lc rgb 'red'
set output 'other.eps'

set output 'SingleDAG.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (ms)'
plot "CommonEntry/MONTAGE.n.100.0.dax.mulT: 1000 mulD: 100" using ($1):($2) title 'Montage-100 ops' with points pt 8 lc rgb 'green',\
"CommonEntry/LIGO.n.50.0.dax.mulT: 1000 mulD: 100" using ($1):($2) title 'LIGO-50 ops' with points pt 2 lc rgb 'red'
set output 'other.eps'


set output 'SingleDAGUserPrefer.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (ms)'
plot "UserPrefBased/MONTAGE.n.100.0.dax.mulT: 1000 mulD: 100" using ($1):($2) title 'Montage-100 ops' with points pt 8 lc rgb 'green',\
"CommonEntry/LIGO.n.50.0.dax.mulT: 1000 mulD: 100" using ($1):($2) title 'LIGO-50 ops' with points pt 2 lc rgb 'red'
set output 'other.eps'



