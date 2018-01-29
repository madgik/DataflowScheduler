reset
set terminal eps
set key font ",15" height 3
set xtics out font ",15"
set ytics font ",15" 
set xlabel font ",15" 
set ylabel font ",15" offset -1
set label font ",4"
set offset 0, 0.05, graph 0.25, 0.0
set xrange [*:*]
set yrange [*:*]
set pointsize 1.2

system "mkdir plots"



set output 'plots/minSubdagTime.eps'
set xlabel 'Total Money ($)'
set ylabel 'Execution Time'
plot "BiObj/ensemble.dat" using ($1):($5) title 'BiObj' with points pt 8 lc rgb 'blue',\
"MultiObj/ensemble.dat" using ($1):($5) title 'MultiObj' with points pt 2 lc rgb 'green',\
"BiObjU/ensemble.dat" using ($1):($5) title 'BiObj- minU' with points pt 1 lc rgb 'red'
set output 'plots/others.eps'


set output 'plots/skylineSubDag.eps'
set xlabel 'Total Money ($)'
set ylabel 'Execution Time (s)'
plot "BiObj/ensemble.dat" using ($3):($4) title 'BiObj' with points pt 8 lc rgb 'blue',\
"MultiObj/ensemble.dat" using ($3):($4) title 'MultiObj' with points pt 2 lc rgb 'green',\
"BiObjU/ensemble.dat" using ($3):($4) title 'BiObj- minU' with points pt 1 lc rgb 'red'
set output 'plots/others.eps'


set output 'plots/skyline.eps'
set xlabel 'Total Money ($)'
set ylabel 'Execution Time (min)'
plot "BiObj/ensemble.dat" using ($1):($2/60000) title 'BiObj' with points pt 8 lc rgb 'blue',\
"MultiObj/ensemble.dat" using ($1):($2/60000) title 'MultiObj' with points pt 2 lc rgb 'green',\
"BiObjU/ensemble.dat" using ($1):($2/60000) title 'BiObj- minU' with points pt 1 lc rgb 'red'
set output 'plots/others.eps'


set output 'plots/meanSubdagTime.eps'
set xlabel 'Total Money ($)'
set ylabel 'Execution Time'
plot "BiObj/ensemble.dat" using ($1):($4) title 'BiObj' with points pt 8 lc rgb 'blue',\
"MultiObj/ensemble.dat" using ($1):($4) title 'MultiObj' with points pt 2 lc rgb 'green',\
"BiObjU/ensemble.dat" using ($1):($4) title 'BiObj- minU' with points pt 1 lc rgb 'red'
set output 'plots/others.eps'

set output 'plots/unfairnessToMoney.eps'
set xlabel 'Total Money ($)'
set ylabel 'Unfairness'
plot "BiObj/ensemble.dat" using ($1):($7) title 'BiObj' with points pt 8 lc rgb 'blue',\
"MultiObj/ensemble.dat" using ($1):($7) title 'MultiObj' with points pt 2 lc rgb 'green',\
"BiObjU/ensemble.dat" using ($1):($7) title 'BiObj- minU' with points pt 1 lc rgb 'red'
set output 'plots/others.eps'

set output 'plots/unfairnessToTime.eps'
set xlabel 'Execution Time (min)'
set ylabel 'Unfairness'
plot "BiObj/ensemble.dat" using ($7):($2/60000) title 'BiObj' with points pt 8 lc rgb 'blue',\
"MultiObj/ensemble.dat" using ($7):($2/60000) title 'MultiObj' with points pt 2 lc rgb 'green',\
"BiObjU/ensemble.dat" using ($7):($2/60000) title 'BiObj- minU' with points pt 1 lc rgb 'red'
set output 'plots/others.eps'


set output 'plots/maxSubdagTime.eps'
set xlabel 'Total Money ($)'
set ylabel 'Execution Time'
plot "BiObj/ensemble.dat" using ($1):($6) title 'BiObj' with points pt 8 lc rgb 'blue',\
"MultiObj/ensemble.dat" using ($1):($6) title 'MultiObj' with points pt 2 lc rgb 'green',\
"BiObjU/ensemble.dat" using ($1):($6) title 'BiObj- minU' with points pt 1 lc rgb 'red'
set output 'plots/others.eps'

